package com.bookmyshow.movie_booking.dto.response;

import com.bookmyshow.movie_booking.enums.BookingStatus;
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
public class BookingResponse {
    private Long id;
    private String referenceId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String showDate;
    private String showTime;
    private List<String> seatCodes;
    private Double totalAmount;
    private BookingStatus bookingStatus;
    private String stripePaymentUrl;
    private String qrCodeUrl;
    private LocalDateTime createdAt;
}