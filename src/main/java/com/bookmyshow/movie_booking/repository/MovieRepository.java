package com.bookmyshow.movie_booking.repository;

import com.bookmyshow.movie_booking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByActiveTrue();
    List<Movie> findByTitleContainingIgnoreCase(String title);
}