package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.dto.request.LoginRequest;
import com.bookmyshow.movie_booking.dto.request.RegisterRequest;
import com.bookmyshow.movie_booking.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}