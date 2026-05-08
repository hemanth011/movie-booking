package com.bookmyshow.movie_booking.controller;

import com.bookmyshow.movie_booking.dto.response.ApiResponse;
import com.bookmyshow.movie_booking.dto.response.SearchResponse;
import com.bookmyshow.movie_booking.dto.response.SeatMapResponse;
import com.bookmyshow.movie_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final UserService userService;

    /**
     * GET /api/public/search?city=Hyderabad&title=RRR
     * Public movie search — no login required.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchResponse>>> searchMovies(
            @RequestParam String city,
            @RequestParam String title) {
        log.info("Public search - movie: '{}' in city: '{}'", title, city);
        return ResponseEntity.ok(ApiResponse.success("Search results",
                userService.searchMovies(city, title)));
    }

    /**
     * GET /api/public/shows/{showId}/seats
     * Public seat map — no login required to view seats.
     */
    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<ApiResponse<SeatMapResponse>> getSeatMap(
            @PathVariable Long showId) {
        log.info("Public seat map for show: {}", showId);
        return ResponseEntity.ok(ApiResponse.success("Seat map fetched",
                userService.getSeatMap(showId)));
    }

    /**
     * GET /api/public/now-playing
     * Returns all active shows grouped by movie.
     * No login required.
     */
    @GetMapping("/now-playing")
    public ResponseEntity<ApiResponse<List<SearchResponse>>> getNowPlaying() {
        log.info("Public now playing request");
        return ResponseEntity.ok(ApiResponse.success("Now playing",
                userService.getNowPlaying()));
    }
}