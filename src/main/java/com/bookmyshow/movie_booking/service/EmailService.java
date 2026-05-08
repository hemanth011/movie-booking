package com.bookmyshow.movie_booking.service;

public interface EmailService {
    void sendBookingConfirmation(String toEmail, String userName,
                                 String movieTitle, String theatreName,
                                 String screenName, String showDate,
                                 String showTime, String seats,
                                 String referenceId, Double totalAmount,
                                 String qrCodeBase64);
}