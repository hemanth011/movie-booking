package com.bookmyshow.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private String referenceId;
    private String userEmail;
    private String userName;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String showDate;
    private String showTime;
    private List<String> seatCodes;
    private Double totalAmount;
    private String eventType; // CONFIRMED, CANCELLED, EXPIRED
    private LocalDateTime eventTime;
}