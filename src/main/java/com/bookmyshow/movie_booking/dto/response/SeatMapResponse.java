package com.bookmyshow.movie_booking.dto.response;

import com.bookmyshow.movie_booking.enums.SeatStatus;
import com.bookmyshow.movie_booking.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponse {
    private Long showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private List<SeatInfo> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private Long showSeatId;
        private String seatCode;    // A1, B3 etc.
        private String rowLabel;
        private Integer seatNumber;
        private SeatType seatType;  // SILVER, GOLD, PLATINUM
        private Double price;
        private SeatStatus seatStatus; // AVAILABLE, LOCKED, BOOKED
    }
}