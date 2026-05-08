package com.bookmyshow.movie_booking.controller;

import com.bookmyshow.movie_booking.dto.request.BookingRequest;
import com.bookmyshow.movie_booking.dto.response.ApiResponse;
import com.bookmyshow.movie_booking.dto.response.BookingResponse;
import com.bookmyshow.movie_booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /api/user/bookings/initiate
     * User selects seats → locks them → creates Stripe PaymentIntent.
     * Returns client secret for frontend to complete payment.
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<BookingResponse>> initiateBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("User {} initiating booking", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Booking initiated",
                bookingService.initiateBooking(request, userDetails.getUsername())));
    }

    /**
     * POST /api/user/bookings/confirm?paymentIntentId=pi_xxx
     * Called after Stripe payment is completed on frontend.
     * Confirms booking, marks seats BOOKED, generates QR code.
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(
            @RequestParam String paymentIntentId) {
        log.info("Confirming payment for intent: {}", paymentIntentId);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed",
                bookingService.confirmPayment(paymentIntentId)));
    }

    /**
     * GET /api/user/bookings
     * Get all bookings for the logged-in user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("User {} fetching their bookings", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched",
                bookingService.getMyBookings(userDetails.getUsername())));
    }

    /**
     * GET /api/user/bookings/{referenceId}
     * Get booking details by reference ID.
     */
    @GetMapping("/{referenceId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @PathVariable String referenceId) {
        log.info("Fetching booking by reference: {}", referenceId);
        return ResponseEntity.ok(ApiResponse.success("Booking fetched",
                bookingService.getBookingByReference(referenceId)));
    }
}