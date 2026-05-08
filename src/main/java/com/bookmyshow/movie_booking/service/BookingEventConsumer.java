package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.config.KafkaConfig;
import com.bookmyshow.movie_booking.dto.BookingEvent;
import com.bookmyshow.movie_booking.repository.BookingRepository;
import com.bookmyshow.movie_booking.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final EmailService emailService;
    private final BookingRepository bookingRepository;

    /**
     * Listens for confirmed booking events.
     * Triggers email with QR code to user.
     */
    @KafkaListener(
            topics = KafkaConfig.BOOKING_CONFIRMED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBookingConfirmed(BookingEvent event) {
        log.info("=== BOOKING CONFIRMED EVENT RECEIVED ===");
        log.info("Reference: {} | User: {}", event.getReferenceId(), event.getUserEmail());

        // Fetch QR code from DB
        bookingRepository.findByReferenceId(event.getReferenceId())
                .ifPresent(booking -> {
                    log.info("Sending confirmation email for: {}", event.getReferenceId());
                    emailService.sendBookingConfirmation(
                            event.getUserEmail(),
                            event.getUserName(),
                            event.getMovieTitle(),
                            event.getTheatreName(),
                            event.getScreenName(),
                            event.getShowDate(),
                            event.getShowTime(),
                            String.join(", ", event.getSeatCodes()),
                            event.getReferenceId(),
                            event.getTotalAmount(),
                            booking.getQrCodeUrl()
                    );
                });

        log.info("========================================");
    }

    /**
     * Listens for cancelled booking events.
     */
    @KafkaListener(
            topics = KafkaConfig.BOOKING_CANCELLED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBookingCancelled(BookingEvent event) {
        log.info("=== BOOKING CANCELLED EVENT RECEIVED ===");
        log.info("Reference: {} | User: {}", event.getReferenceId(), event.getUserEmail());
        log.info("========================================");
    }

    /**
     * Listens for expired booking events.
     */
    @KafkaListener(
            topics = KafkaConfig.BOOKING_EXPIRED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBookingExpired(BookingEvent event) {
        log.info("=== BOOKING EXPIRED EVENT RECEIVED ===");
        log.info("Reference: {} | User: {}", event.getReferenceId(), event.getUserEmail());
        log.info("======================================");
    }
}