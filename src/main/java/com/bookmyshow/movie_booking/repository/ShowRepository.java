package com.bookmyshow.movie_booking.repository;

import com.bookmyshow.movie_booking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByScreenId(Long screenId);

    // Find shows by movie and city for user search
    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId " +
            "AND s.screen.theatre.city = :city " +
            "AND s.showDate = :date " +
            "AND s.active = true")
    List<Show> findByMovieAndCityAndDate(@Param("movieId") Long movieId,
                                         @Param("city") String city,

                                         @Param("date") LocalDate date);
    @Query("SELECT s FROM Show s WHERE s.active = true AND s.showDate >= CURRENT_DATE ORDER BY s.showDate ASC")
    List<Show> findNowPlayingShows();

    // Find shows by movie title and city for search
    @Query("SELECT s FROM Show s WHERE LOWER(s.movie.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND LOWER(s.screen.theatre.city) = LOWER(:city) " +
            "AND s.active = true")
    List<Show> findByMovieTitleAndCity(@Param("title") String title,
                                       @Param("city") String city);
}