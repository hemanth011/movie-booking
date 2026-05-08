package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.dto.request.BookingRequest;
import com.bookmyshow.movie_booking.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse initiateBooking(BookingRequest request, String userEmail);
    BookingResponse confirmPayment(String paymentIntentId);
    void cancelExpiredBookings();
    List<BookingResponse> getMyBookings(String userEmail);
    BookingResponse getBookingByReference(String referenceId);
}