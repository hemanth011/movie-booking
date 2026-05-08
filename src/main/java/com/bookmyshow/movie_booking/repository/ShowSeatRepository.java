package com.bookmyshow.movie_booking.repository;

import com.bookmyshow.movie_booking.entity.ShowSeat;
import com.bookmyshow.movie_booking.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowId(Long showId);
    List<ShowSeat> findByShowIdAndSeatStatus(Long showId, SeatStatus status);
    Optional<ShowSeat> findByShowIdAndSeatLayoutId(Long showId, Long seatLayoutId);
    List<ShowSeat> findByBookingId(Long bookingId);
    void deleteByShowId(Long showId);
}