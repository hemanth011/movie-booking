package com.bookmyshow.movie_booking.repository;

import com.bookmyshow.movie_booking.entity.SeatLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatLayoutRepository extends JpaRepository<SeatLayout, Long> {
    List<SeatLayout> findByScreenId(Long screenId);
    void deleteByScreenId(Long screenId);
}