package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.dto.response.SearchResponse;
import com.bookmyshow.movie_booking.dto.response.SeatMapResponse;

import java.util.List;

public interface UserService {
    List<SearchResponse> searchMovies(String city, String title);
    SeatMapResponse getSeatMap(Long showId);
    List<SearchResponse> getNowPlaying();
}