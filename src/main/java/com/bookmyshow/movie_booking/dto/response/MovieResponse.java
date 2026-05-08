package com.bookmyshow.movie_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private String language;
    private String genre;
    private Integer duration;
    private String cast;
    private String posterUrl;
    private LocalDate releaseDate;
    private boolean active;
}