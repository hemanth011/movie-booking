package com.bookmyshow.movie_booking.config;

import com.bookmyshow.movie_booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingService bookingService;

    /**
     * Runs every 5 minutes to expire pending bookings
     * whose Redis seat locks have already expired.
     */
    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void expireBookings() {
        log.info("Running booking expiry scheduler");
        bookingService.cancelExpiredBookings();
    }
}