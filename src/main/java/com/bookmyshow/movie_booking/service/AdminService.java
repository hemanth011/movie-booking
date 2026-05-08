package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.dto.request.MovieRequest;
import com.bookmyshow.movie_booking.dto.response.DashboardResponse;
import com.bookmyshow.movie_booking.dto.response.MovieResponse;
import com.bookmyshow.movie_booking.dto.response.UserResponse;

import java.util.List;

public interface AdminService {
    void approveTheatreOwner(Long ownerId);
    void rejectTheatreOwner(Long ownerId);
    List<UserResponse> getPendingTheatreOwners();
    List<UserResponse> getAllUsers();
    void toggleUserStatus(Long userId);
    MovieResponse addMovie(MovieRequest request);
    MovieResponse updateMovie(Long movieId, MovieRequest request);
    void deleteMovie(Long movieId);
    List<MovieResponse> getAllMovies();
    DashboardResponse getMonthlyDashboard();
}