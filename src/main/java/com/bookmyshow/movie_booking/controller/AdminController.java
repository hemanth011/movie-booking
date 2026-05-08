package com.bookmyshow.movie_booking.controller;

import com.bookmyshow.movie_booking.dto.request.MovieRequest;
import com.bookmyshow.movie_booking.dto.response.ApiResponse;
import com.bookmyshow.movie_booking.dto.response.DashboardResponse;
import com.bookmyshow.movie_booking.dto.response.MovieResponse;
import com.bookmyshow.movie_booking.dto.response.UserResponse;
import com.bookmyshow.movie_booking.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ─── Theatre Owner Approval ───────────────────────────────────

    /** GET /api/admin/owners/pending — Get all pending theatre owners */
    @GetMapping("/owners/pending")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPendingOwners() {
        log.info("Admin fetching pending theatre owners");
        return ResponseEntity.ok(ApiResponse.success("Pending owners fetched",
                adminService.getPendingTheatreOwners()));
    }

    /** PUT /api/admin/owners/{id}/approve — Approve a theatre owner */
    @PutMapping("/owners/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveOwner(@PathVariable Long id) {
        log.info("Admin approving theatre owner id: {}", id);
        adminService.approveTheatreOwner(id);
        return ResponseEntity.ok(ApiResponse.success("Theatre owner approved", null));
    }

    /** PUT /api/admin/owners/{id}/reject — Reject a theatre owner */
    @PutMapping("/owners/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectOwner(@PathVariable Long id) {
        log.info("Admin rejecting theatre owner id: {}", id);
        adminService.rejectTheatreOwner(id);
        return ResponseEntity.ok(ApiResponse.success("Theatre owner rejected", null));
    }

    // ─── User Management ─────────────────────────────────────────

    /** GET /api/admin/users — Get all users */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Admin fetching all users");
        return ResponseEntity.ok(ApiResponse.success("Users fetched",
                adminService.getAllUsers()));
    }

    /** PUT /api/admin/users/{id}/toggle — Enable or disable a user */
    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleUser(@PathVariable Long id) {
        log.info("Admin toggling user status for id: {}", id);
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated", null));
    }

    // ─── Movie Management ─────────────────────────────────────────

    /** POST /api/admin/movies — Add a new movie */
    @PostMapping("/movies")
    public ResponseEntity<ApiResponse<MovieResponse>> addMovie(
            @Valid @RequestBody MovieRequest request) {
        log.info("Admin adding movie: {}", request.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Movie added",
                adminService.addMovie(request)));
    }

    /** PUT /api/admin/movies/{id} — Update a movie */
    @PutMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {
        log.info("Admin updating movie id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Movie updated",
                adminService.updateMovie(id, request)));
    }

    /** DELETE /api/admin/movies/{id} — Soft delete a movie */
    @DeleteMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
        log.info("Admin deleting movie id: {}", id);
        adminService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Movie deleted", null));
    }

    /** GET /api/admin/movies — Get all active movies */
    @GetMapping("/movies")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies() {
        log.info("Admin fetching all movies");
        return ResponseEntity.ok(ApiResponse.success("Movies fetched",
                adminService.getAllMovies()));
    }

    // ─── Dashboard ────────────────────────────────────────────────

    /** GET /api/admin/dashboard — Monthly stats */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        log.info("Admin fetching monthly dashboard");
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched",
                adminService.getMonthlyDashboard()));
    }
}