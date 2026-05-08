package com.bookmyshow.movie_booking.repository;

import com.bookmyshow.movie_booking.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByOwnerId(Long ownerId);
    List<Theatre> findByCityIgnoreCase(String city);
    List<Theatre> findByActiveTrueAndCityIgnoreCase(String city);
}