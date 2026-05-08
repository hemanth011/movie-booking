package com.bookmyshow.movie_booking.dto.request;

import com.bookmyshow.movie_booking.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatLayoutRequest {

    @NotBlank(message = "Row label is required")
    private String rowLabel; // A, B, C...

    @NotNull(message = "Seat number is required")
    private Integer seatNumber; // 1, 2, 3...

    @NotNull(message = "Seat type is required")
    private SeatType seatType; // SILVER, GOLD, PLATINUM

    @NotNull(message = "Price is required")
    private Double price;
}