package com.bookmyshow.movie_booking.controller;

import com.bookmyshow.movie_booking.dto.response.ApiResponse;
import com.bookmyshow.movie_booking.dto.response.SearchResponse;
import com.bookmyshow.movie_booking.dto.response.SeatMapResponse;
import com.bookmyshow.movie_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class UserController {

    private final UserService userService;

    /**
     * GET /api/user/search?city=Hyderabad&title=RRR
     * Search movies by city and title.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchResponse>>> searchMovies(
            @RequestParam String city,
            @RequestParam String title) {
        log.info("User searching for movie: '{}' in city: '{}'", title, city);
        return ResponseEntity.ok(ApiResponse.success("Search results",
                userService.searchMovies(city, title)));
    }

    /**
     * GET /api/user/shows/{showId}/seats
     * Get live seat map for a show.
     */
    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<ApiResponse<SeatMapResponse>> getSeatMap(
            @PathVariable Long showId) {
        log.info("User fetching seat map for show: {}", showId);
        return ResponseEntity.ok(ApiResponse.success("Seat map fetched",
                userService.getSeatMap(showId)));
    }
}