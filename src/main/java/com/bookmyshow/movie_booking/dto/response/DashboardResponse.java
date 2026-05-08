package com.bookmyshow.movie_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalBookingsThisMonth;
    private Double totalRevenueThisMonth;
    private Long newUsersThisMonth;
    private Map<String, Long> topMoviesByBookings;
    private Map<String, Double> topTheatresByRevenue;
}