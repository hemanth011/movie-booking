package com.bookmyshow.movie_booking.service.impl;

import com.bookmyshow.movie_booking.dto.request.MovieRequest;
import com.bookmyshow.movie_booking.dto.response.DashboardResponse;
import com.bookmyshow.movie_booking.dto.response.MovieResponse;
import com.bookmyshow.movie_booking.dto.response.UserResponse;
import com.bookmyshow.movie_booking.entity.Movie;
import com.bookmyshow.movie_booking.entity.User;
import com.bookmyshow.movie_booking.enums.ApprovalStatus;
import com.bookmyshow.movie_booking.enums.BookingStatus;
import com.bookmyshow.movie_booking.enums.Role;
import com.bookmyshow.movie_booking.exception.ResourceNotFoundException;
import com.bookmyshow.movie_booking.exception.UnauthorizedException;
import com.bookmyshow.movie_booking.repository.BookingRepository;
import com.bookmyshow.movie_booking.repository.MovieRepository;
import com.bookmyshow.movie_booking.repository.UserRepository;
import com.bookmyshow.movie_booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;

    /**
     * Approves a pending theatre owner.
     */
    @Override
    public void approveTheatreOwner(Long ownerId) {
        log.info("Approving theatre owner with id: {}", ownerId);
        User owner = getTheatreOwner(ownerId);
        owner.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(owner);
        log.info("Theatre owner approved: {}", owner.getEmail());
    }

    /**
     * Rejects a pending theatre owner.
     */
    @Override
    public void rejectTheatreOwner(Long ownerId) {
        log.info("Rejecting theatre owner with id: {}", ownerId);
        User owner = getTheatreOwner(ownerId);
        owner.setApprovalStatus(ApprovalStatus.REJECTED);
        userRepository.save(owner);
        log.info("Theatre owner rejected: {}", owner.getEmail());
    }

    /**
     * Returns all theatre owners with PENDING approval status.
     */
    @Override
    public List<UserResponse> getPendingTheatreOwners() {
        log.info("Fetching all pending theatre owners");
        return userRepository.findByRoleAndApprovalStatus(
                        Role.ROLE_THEATRE_OWNER, ApprovalStatus.PENDING)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all users with role ROLE_USER.
     */
    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findByRole(Role.ROLE_USER)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Enables or disables a user account.
     */
    @Override
    public void toggleUserStatus(Long userId) {
        log.info("Toggling status for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("User {} status set to enabled={}", user.getEmail(), user.isEnabled());
    }

    /**
     * Adds a new movie to the system.
     */
    @Override
    public MovieResponse addMovie(MovieRequest request) {
        log.info("Adding new movie: {}", request.getTitle());
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .language(request.getLanguage())
                .genre(request.getGenre())
                .duration(request.getDuration())
                .cast(request.getCast())
                .posterUrl(request.getPosterUrl())
                .releaseDate(request.getReleaseDate())
                .active(true)
                .build();
        Movie saved = movieRepository.save(movie);
        log.info("Movie added successfully with id: {}", saved.getId());
        return mapToMovieResponse(saved);
    }

    /**
     * Updates an existing movie.
     */
    @Override
    public MovieResponse updateMovie(Long movieId, MovieRequest request) {
        log.info("Updating movie with id: {}", movieId);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", movieId));
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setLanguage(request.getLanguage());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setCast(request.getCast());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setReleaseDate(request.getReleaseDate());
        Movie updated = movieRepository.save(movie);
        log.info("Movie updated successfully: {}", updated.getId());
        return mapToMovieResponse(updated);
    }

    /**
     * Soft deletes a movie by setting active = false.
     */
    @Override
    public void deleteMovie(Long movieId) {
        log.info("Soft deleting movie with id: {}", movieId);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", movieId));
        movie.setActive(false);
        movieRepository.save(movie);
        log.info("Movie soft deleted: {}", movieId);
    }

    /**
     * Returns all active movies.
     */
    @Override
    public List<MovieResponse> getAllMovies() {
        log.info("Fetching all active movies");
        return movieRepository.findByActiveTrue()
                .stream()
                .map(this::mapToMovieResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns monthly dashboard stats for admin.
     */
    @Override
    public DashboardResponse getMonthlyDashboard() {
        log.info("Generating monthly dashboard stats");

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        Long totalBookings = bookingRepository.countBookingsBetween(
                startOfMonth, now, BookingStatus.CONFIRMED);

        Double totalRevenue = bookingRepository.sumRevenueBetween(
                startOfMonth, now, BookingStatus.CONFIRMED);

        Long newUsers = userRepository.countByRoleAndCreatedAtBetween(
                Role.ROLE_USER, startOfMonth, now);

        // Top 5 movies
        Map<String, Long> topMovies = new LinkedHashMap<>();
        bookingRepository.findTopMoviesBetween(startOfMonth, now)
                .stream().limit(5)
                .forEach(row -> topMovies.put((String) row[0], (Long) row[1]));

        // Top 5 theatres
        Map<String, Double> topTheatres = new LinkedHashMap<>();
        bookingRepository.findTopTheatresBetween(startOfMonth, now)
                .stream().limit(5)
                .forEach(row -> topTheatres.put((String) row[0], (Double) row[1]));

        log.info("Dashboard generated - Bookings: {}, Revenue: {}", totalBookings, totalRevenue);

        return DashboardResponse.builder()
                .totalBookingsThisMonth(totalBookings)
                .totalRevenueThisMonth(totalRevenue)
                .newUsersThisMonth(newUsers)
                .topMoviesByBookings(topMovies)
                .topTheatresByRevenue(topTheatres)
                .build();
    }

    // ─── Private Helpers ──────────────────────────────────────────

    private User getTheatreOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre owner", ownerId));
        if (owner.getRole() != Role.ROLE_THEATRE_OWNER) {
            throw new UnauthorizedException("User is not a theatre owner");
        }
        return owner;
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .approvalStatus(user.getApprovalStatus() != null
                        ? user.getApprovalStatus().name() : null)
                .enabled(user.isEnabled())
                .build();
    }

    private MovieResponse mapToMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .duration(movie.getDuration())
                .cast(movie.getCast())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .active(movie.isActive())
                .build();
    }
}