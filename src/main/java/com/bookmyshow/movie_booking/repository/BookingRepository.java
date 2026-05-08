package com.bookmyshow.movie_booking.repository;

import com.bookmyshow.movie_booking.entity.Booking;
import com.bookmyshow.movie_booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReferenceId(String referenceId);

    List<Booking> findByUserId(Long userId);

    // Total bookings between dates
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :start AND :end AND b.bookingStatus = :status")
    Long countBookingsBetween(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("status") BookingStatus status);

    // Total revenue between dates
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.createdAt BETWEEN :start AND :end AND b.bookingStatus = :status")
    Double sumRevenueBetween(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("status") BookingStatus status);

    // Top movies by booking count
    @Query("SELECT b.show.movie.title, COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :start AND :end GROUP BY b.show.movie.title ORDER BY COUNT(b) DESC")
    List<Object[]> findTopMoviesBetween(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // Top theatres by revenue
    @Query("SELECT b.show.screen.theatre.name, COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.createdAt BETWEEN :start AND :end GROUP BY b.show.screen.theatre.name ORDER BY SUM(b.totalAmount) DESC")
    List<Object[]> findTopTheatresBetween(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}