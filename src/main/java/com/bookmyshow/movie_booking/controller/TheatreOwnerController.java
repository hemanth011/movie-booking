package com.bookmyshow.movie_booking.controller;

import com.bookmyshow.movie_booking.dto.request.ScreenRequest;
import com.bookmyshow.movie_booking.dto.request.SeatLayoutRequest;
import com.bookmyshow.movie_booking.dto.request.ShowRequest;
import com.bookmyshow.movie_booking.dto.request.TheatreRequest;
import com.bookmyshow.movie_booking.dto.response.*;
import com.bookmyshow.movie_booking.dto.response.TheatreResponse;
import com.bookmyshow.movie_booking.service.TheatreOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_THEATRE_OWNER')")
public class TheatreOwnerController {

    private final TheatreOwnerService theatreOwnerService;

    // ─── Theatre ──────────────────────────────────────────────────

    /** POST /api/owner/theatres — Add a new theatre */
    @PostMapping("/theatres")
    public ResponseEntity<ApiResponse<TheatreResponse>> addTheatre(
            @Valid @RequestBody TheatreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Owner {} adding theatre", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Theatre added",
                theatreOwnerService.addTheatre(request, userDetails.getUsername())));
    }

    /** PUT /api/owner/theatres/{id} — Update a theatre */
    @PutMapping("/theatres/{id}")
    public ResponseEntity<ApiResponse<TheatreResponse>> updateTheatre(
            @PathVariable Long id,
            @Valid @RequestBody TheatreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Owner {} updating theatre id: {}", userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Theatre updated",
                theatreOwnerService.updateTheatre(id, request, userDetails.getUsername())));
    }

    /** GET /api/owner/theatres — Get my theatres */
    @GetMapping("/theatres")
    public ResponseEntity<ApiResponse<List<TheatreResponse>>> getMyTheatres(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Owner {} fetching their theatres", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Theatres fetched",
                theatreOwnerService.getMyTheatres(userDetails.getUsername())));
    }

    // ─── Screen ───────────────────────────────────────────────────

    /** POST /api/owner/theatres/{theatreId}/screens — Add a screen */
    @PostMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<ApiResponse<ScreenResponse>> addScreen(
            @PathVariable Long theatreId,
            @Valid @RequestBody ScreenRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Owner {} adding screen to theatre: {}", userDetails.getUsername(), theatreId);
        return ResponseEntity.ok(ApiResponse.success("Screen added",
                theatreOwnerService.addScreen(theatreId, request, userDetails.getUsername())));
    }

    /** GET /api/owner/theatres/{theatreId}/screens — Get screens */
    @GetMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<ApiResponse<List<ScreenResponse>>> getScreens(
            @PathVariable Long theatreId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Screens fetched",
                theatreOwnerService.getScreensByTheatre(theatreId, userDetails.getUsername())));
    }

    // ─── Seat Layout ──────────────────────────────────────────────

    /** POST /api/owner/screens/{screenId}/seats — Save seat layout */
    @PostMapping("/screens/{screenId}/seats")
    public ResponseEntity<ApiResponse<List<SeatLayoutResponse>>> saveSeatLayout(
            @PathVariable Long screenId,
            @RequestBody List<@Valid SeatLayoutRequest> requests,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Owner {} saving seat layout for screen: {}", userDetails.getUsername(), screenId);
        return ResponseEntity.ok(ApiResponse.success("Seat layout saved",
                theatreOwnerService.saveSeatLayout(screenId, requests, userDetails.getUsername())));
    }

    /** GET /api/owner/screens/{screenId}/seats — Get seat layout */
    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<ApiResponse<List<SeatLayoutResponse>>> getSeatLayout(
            @PathVariable Long screenId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Seat layout fetched",
                theatreOwnerService.getSeatLayout(screenId, userDetails.getUsername())));
    }

    // ─── Shows ────────────────────────────────────────────────────

    /** POST /api/owner/shows — Add a show */
    @PostMapping("/shows")
    public ResponseEntity<ApiResponse<ShowResponse>> addShow(
            @Valid @RequestBody ShowRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Owner {} adding show", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Show added",
                theatreOwnerService.addShow(request, userDetails.getUsername())));
    }

    /** GET /api/owner/shows — Get my shows */
    @GetMapping("/shows")
    public ResponseEntity<ApiResponse<List<ShowResponse>>> getMyShows(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Shows fetched",
                theatreOwnerService.getMyShows(userDetails.getUsername())));
    }

    /** DELETE /api/owner/shows/{showId} — Cancel a show */
    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<ApiResponse<Void>> cancelShow(
            @PathVariable Long showId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Owner {} cancelling show: {}", userDetails.getUsername(), showId);
        theatreOwnerService.cancelShow(showId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Show cancelled", null));
    }

    // ─── Movies ───────────────────────────────────────────────────

    /** GET /api/owner/movies — Get available movies from admin */
    @GetMapping("/movies")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAvailableMovies() {
        log.info("Theatre owner fetching available movies");
        return ResponseEntity.ok(ApiResponse.success("Movies fetched",
                theatreOwnerService.getAvailableMovies()));
    }
}