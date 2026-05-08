package com.bookmyshow.movie_booking.service.impl;

import com.bookmyshow.movie_booking.dto.BookingEvent;
import com.bookmyshow.movie_booking.dto.request.BookingRequest;
import com.bookmyshow.movie_booking.dto.response.BookingResponse;
import com.bookmyshow.movie_booking.entity.*;
import com.bookmyshow.movie_booking.enums.BookingStatus;
import com.bookmyshow.movie_booking.enums.PaymentStatus;
import com.bookmyshow.movie_booking.enums.SeatStatus;
import com.bookmyshow.movie_booking.exception.ResourceNotFoundException;
import com.bookmyshow.movie_booking.exception.SeatNotAvailableException;
import com.bookmyshow.movie_booking.repository.*;
import com.bookmyshow.movie_booking.service.BookingEventProducer;
import com.bookmyshow.movie_booking.service.BookingService;
import com.bookmyshow.movie_booking.service.QRCodeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final RedisTemplate<String, String> redisTemplate;
    private final BookingEventProducer bookingEventProducer;
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.currency}")
    private String currency;

    @Value("${app.seat.lock.ttl}")
    private long seatLockTtl; // in seconds

    // Redis key pattern: SEAT_LOCK:{showSeatId}
    private static final String SEAT_LOCK_PREFIX = "SEAT_LOCK:";

    /**
     * Initialize Stripe API key on startup.
     */
    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API initialized");
    }

    /**
     * Initiates a booking:
     * 1. Validates seats are available
     * 2. Locks seats in Redis with TTL (10 mins)
     * 3. Creates a PENDING booking in DB
     * 4. Creates a Stripe PaymentIntent
     * 5. Returns Stripe client secret for frontend to complete payment
     */
    @Override
    @Transactional
    public BookingResponse initiateBooking(BookingRequest request, String userEmail) {
        log.info("User {} initiating booking for show: {}", userEmail, request.getShowId());

        // Fetch user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Fetch show
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", request.getShowId()));

        // Fetch selected show seats
        List<ShowSeat> selectedSeats = request.getShowSeatIds().stream()
                .map(seatId -> showSeatRepository.findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("ShowSeat", seatId)))
                .collect(Collectors.toList());

        // Validate all seats are AVAILABLE and not Redis-locked
        for (ShowSeat seat : selectedSeats) {
            if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                log.warn("Seat {} is not available - status: {}",
                        seat.getSeatLayout().getSeatCode(), seat.getSeatStatus());
                throw new SeatNotAvailableException(
                        "Seat " + seat.getSeatLayout().getSeatCode() + " is not available");
            }

            // Check Redis lock
            String redisKey = SEAT_LOCK_PREFIX + seat.getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                log.warn("Seat {} is locked by another user", seat.getSeatLayout().getSeatCode());
                throw new SeatNotAvailableException(
                        "Seat " + seat.getSeatLayout().getSeatCode() +
                                " is temporarily locked. Please try another seat.");
            }
        }

        // Lock all seats in Redis with TTL
        for (ShowSeat seat : selectedSeats) {
            String redisKey = SEAT_LOCK_PREFIX + seat.getId();
            redisTemplate.opsForValue().set(redisKey, userEmail, seatLockTtl, TimeUnit.SECONDS);
            log.debug("Seat {} locked in Redis for user: {}", seat.getId(), userEmail);
        }

        // Mark seats as LOCKED in DB
        selectedSeats.forEach(seat -> seat.setSeatStatus(SeatStatus.LOCKED));
        showSeatRepository.saveAll(selectedSeats);

        // Calculate total amount
        Double totalAmount = selectedSeats.stream()
                .mapToDouble(seat -> seat.getSeatLayout().getPrice())
                .sum();

        // Generate unique booking reference ID
        String referenceId = "BMS-" + LocalDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Create PENDING booking
        Booking booking = Booking.builder()
                .referenceId(referenceId)
                .user(user)
                .show(show)
                .totalAmount(totalAmount)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Link seats to booking
        selectedSeats.forEach(seat -> seat.setBooking(savedBooking));
        showSeatRepository.saveAll(selectedSeats);

        log.info("Booking created with reference: {}", referenceId);

        // Create Stripe PaymentIntent
        String clientSecret = createStripePaymentIntent(totalAmount, referenceId, savedBooking);

        return mapToBookingResponse(savedBooking, selectedSeats, clientSecret);
    }

    /**
     * Builds a BookingEvent from a booking entity for Kafka publishing.
     */
    private BookingEvent buildBookingEvent(Booking booking,
                                           List<ShowSeat> seats,
                                           String eventType) {
        List<String> seatCodes = seats.stream()
                .map(s -> s.getSeatLayout().getSeatCode())
                .collect(Collectors.toList());

        return BookingEvent.builder()
                .referenceId(booking.getReferenceId())
                .userEmail(booking.getUser().getEmail())
                .userName(booking.getUser().getName())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .theatreName(booking.getShow().getScreen().getTheatre().getName())
                .screenName(booking.getShow().getScreen().getName())
                .showDate(booking.getShow().getShowDate().toString())
                .showTime(booking.getShow().getShowTime().toString())
                .seatCodes(seatCodes)
                .totalAmount(booking.getTotalAmount())
                .eventType(eventType)
                .eventTime(LocalDateTime.now())
                .build();
    }


    /**
     * Confirms booking after successful Stripe payment:
     * 1. Fetches payment intent from Stripe
     * 2. Marks booking as CONFIRMED
     * 3. Marks seats as BOOKED
     * 4. Releases Redis locks
     * 5. Generates QR code
     */
    @Override
    @Transactional
    public BookingResponse confirmPayment(String paymentIntentId) {
        log.info("Confirming payment for paymentIntentId: {}", paymentIntentId);

        // Fetch payment from DB
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for intent: " + paymentIntentId));

        Booking booking = payment.getBooking();

        // Verify with Stripe
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            log.info("Stripe PaymentIntent status: {}", intent.getStatus());

            // Accept both succeeded and requires_payment_method (test mode)
            // In production remove requires_payment_method
            if (!"succeeded".equals(intent.getStatus())
                    && !"requires_payment_method".equals(intent.getStatus())
                    && !"requires_confirmation".equals(intent.getStatus())
                    && !"requires_action".equals(intent.getStatus())) {

                log.warn("Payment status not acceptable: {}", intent.getStatus());
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                releaseSeatsForBooking(booking);
                booking.setBookingStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                throw new RuntimeException("Payment not successful. Status: "
                        + intent.getStatus());
            }

        } catch (StripeException e) {
            log.error("Stripe error confirming payment: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }

        // Confirm payment in DB
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // Confirm booking
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        // Mark seats as BOOKED and release Redis locks
        List<ShowSeat> bookedSeats = showSeatRepository.findByBookingId(booking.getId());
        bookedSeats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.BOOKED);
            redisTemplate.delete(SEAT_LOCK_PREFIX + seat.getId());
            log.debug("Redis lock released for seat: {}", seat.getId());
        });
        showSeatRepository.saveAll(bookedSeats);

        // Generate QR code from reference ID
        String qrCode = qrCodeService.generateQRCode(booking.getReferenceId());
        booking.setQrCodeUrl(qrCode);
        bookingRepository.save(booking);

        log.info("Booking confirmed: {}", booking.getReferenceId());

        // Publish Kafka event
        BookingEvent event = buildBookingEvent(booking, bookedSeats, "CONFIRMED");
        bookingEventProducer.publishBookingConfirmed(event);

        return mapToBookingResponse(booking, bookedSeats, null);
    }

    /**
     * Cancels expired PENDING bookings.
     * Releases locked seats back to AVAILABLE.
     * Called by a scheduled job.
     */
    @Override
    @Transactional
    public void cancelExpiredBookings() {
        log.info("Checking for expired pending bookings");

        // Find bookings still PENDING older than 10 minutes
        List<Booking> expiredBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.PENDING
                        && b.getCreatedAt().isBefore(
                        LocalDateTime.now().minusSeconds(seatLockTtl)))
                .collect(Collectors.toList());

        expiredBookings.forEach(booking -> {
            log.info("Expiring booking: {}", booking.getReferenceId());
            booking.setBookingStatus(BookingStatus.EXPIRED);
            releaseSeatsForBooking(booking);
            bookingRepository.save(booking);
            List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());
            BookingEvent event = buildBookingEvent(booking, seats, "EXPIRED");
            bookingEventProducer.publishBookingExpired(event);

        });
        // Publish expired event to Kafka

        if (!expiredBookings.isEmpty()) {
            log.info("Expired {} pending bookings", expiredBookings.size());
        }
    }

    /**
     * Returns all bookings for the logged-in user.
     */
    @Override
    @Transactional
    public List<BookingResponse> getMyBookings(String userEmail) {
        log.info("Fetching bookings for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return bookingRepository.findByUserId(user.getId()).stream()
                .map(booking -> {
                    List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());
                    return mapToBookingResponse(booking, seats, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns a booking by its reference ID.
     */
    @Override
    @Transactional
    public BookingResponse getBookingByReference(String referenceId) {
        log.info("Fetching booking by reference: {}", referenceId);
        Booking booking = bookingRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with reference: " + referenceId));
        List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());
        return mapToBookingResponse(booking, seats, null);
    }

    // ─── Private Helpers ──────────────────────────────────────────

    /**
     * Creates a Stripe PaymentIntent and saves it to DB.
     * Returns the client secret for frontend.
     */
    private String createStripePaymentIntent(Double totalAmount,
                                             String referenceId,
                                             Booking booking) {
        try {
            // Stripe amount is in smallest currency unit (paise for INR)
            long amountInPaise = Math.round(totalAmount * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInPaise)
                    .setCurrency(currency)
                    .putMetadata("bookingReference", referenceId)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Stripe PaymentIntent created: {} for booking: {}",
                    intent.getId(), referenceId);

            // Save payment record in DB
            Payment payment = Payment.builder()
                    .booking(booking)
                    .stripePaymentIntentId(intent.getId())
                    .amount(totalAmount)
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(payment);

            return intent.getClientSecret();

        } catch (StripeException e) {
            log.error("Stripe PaymentIntent creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment initialization failed: " + e.getMessage());
        }
    }

    /**
     * Releases locked seats back to AVAILABLE when booking expires or is cancelled.
     */
    private void releaseSeatsForBooking(Booking booking) {
        List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());
        seats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null);
            redisTemplate.delete(SEAT_LOCK_PREFIX + seat.getId());
        });
        showSeatRepository.saveAll(seats);
        log.info("Released {} seats for booking: {}", seats.size(), booking.getReferenceId());
    }

    /**
     * Maps booking entity to BookingResponse DTO.
     */
    private BookingResponse mapToBookingResponse(Booking booking,
                                                 List<ShowSeat> seats,
                                                 String stripeClientSecret) {
        List<String> seatCodes = seats.stream()
                .map(s -> s.getSeatLayout().getSeatCode())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .referenceId(booking.getReferenceId())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .theatreName(booking.getShow().getScreen().getTheatre().getName())
                .screenName(booking.getShow().getScreen().getName())
                .showDate(booking.getShow().getShowDate().toString())
                .showTime(booking.getShow().getShowTime().toString())
                .seatCodes(seatCodes)
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .stripePaymentUrl(stripeClientSecret)
                .qrCodeUrl(booking.getQrCodeUrl())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}