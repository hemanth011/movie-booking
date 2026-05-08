package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.dto.request.ScreenRequest;
import com.bookmyshow.movie_booking.dto.request.SeatLayoutRequest;
import com.bookmyshow.movie_booking.dto.request.ShowRequest;
import com.bookmyshow.movie_booking.dto.request.TheatreRequest;
import com.bookmyshow.movie_booking.dto.response.*;
import com.bookmyshow.movie_booking.dto.response.TheatreResponse;

import java.util.List;

public interface TheatreOwnerService {
    TheatreResponse addTheatre(TheatreRequest request, String ownerEmail);
    TheatreResponse updateTheatre(Long theatreId, TheatreRequest request, String ownerEmail);
    List<TheatreResponse> getMyTheatres(String ownerEmail);

    ScreenResponse addScreen(Long theatreId, ScreenRequest request, String ownerEmail);
    List<ScreenResponse> getScreensByTheatre(Long theatreId, String ownerEmail);

    List<SeatLayoutResponse> saveSeatLayout(Long screenId, List<SeatLayoutRequest> requests, String ownerEmail);
    List<SeatLayoutResponse> getSeatLayout(Long screenId, String ownerEmail);

    ShowResponse addShow(ShowRequest request, String ownerEmail);
    List<ShowResponse> getMyShows(String ownerEmail);
    void cancelShow(Long showId, String ownerEmail);

    List<MovieResponse> getAvailableMovies();
}