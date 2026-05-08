package com.bookmyshow.movie_booking.service.impl;

import com.bookmyshow.movie_booking.dto.request.LoginRequest;
import com.bookmyshow.movie_booking.dto.request.RegisterRequest;
import com.bookmyshow.movie_booking.dto.response.AuthResponse;
import com.bookmyshow.movie_booking.entity.User;
import com.bookmyshow.movie_booking.enums.ApprovalStatus;
import com.bookmyshow.movie_booking.enums.Role;
import com.bookmyshow.movie_booking.exception.DuplicateResourceException;
import com.bookmyshow.movie_booking.repository.UserRepository;
import com.bookmyshow.movie_booking.service.AuthService;
import com.bookmyshow.movie_booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user, theatre owner, or admin.
     * Theatre owners are set to PENDING approval by default.
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Build user entity
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .enabled(true)
                .build();

        // Theatre owners need admin approval before they can operate
        if (request.getRole() == Role.ROLE_THEATRE_OWNER) {
            user.setApprovalStatus(ApprovalStatus.PENDING);
            log.info("Theatre owner registered - approval status set to PENDING for: {}", request.getEmail());
        }

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getEmail());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    /**
     * Authenticates user credentials and returns JWT token.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Fetch user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found after authentication: {}", request.getEmail());
                    return new RuntimeException("User not found");
                });

        log.info("User logged in successfully: {} with role: {}", user.getEmail(), user.getRole());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}