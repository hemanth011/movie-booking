package com.bookmyshow.movie_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private Long movieId;
    private String movieTitle;
    private String language;
    private String genre;
    private Integer duration;
    private String posterUrl;
    private List<ShowResponse> shows;
}