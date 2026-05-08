package com.bookmyshow.movie_booking.repository;

import com.bookmyshow.movie_booking.entity.User;
import com.bookmyshow.movie_booking.enums.ApprovalStatus;
import com.bookmyshow.movie_booking.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus);
    List<User> findByRole(Role role);
    Long countByRoleAndCreatedAtBetween(Role role, LocalDateTime start, LocalDateTime end);
}