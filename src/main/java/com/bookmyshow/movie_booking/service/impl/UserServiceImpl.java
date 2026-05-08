package com.bookmyshow.movie_booking.service.impl;

import com.bookmyshow.movie_booking.dto.response.SearchResponse;
import com.bookmyshow.movie_booking.dto.response.SeatMapResponse;
import com.bookmyshow.movie_booking.dto.response.ShowResponse;
import com.bookmyshow.movie_booking.entity.Show;
import com.bookmyshow.movie_booking.entity.ShowSeat;
import com.bookmyshow.movie_booking.exception.ResourceNotFoundException;
import com.bookmyshow.movie_booking.repository.ShowRepository;
import com.bookmyshow.movie_booking.repository.ShowSeatRepository;
import com.bookmyshow.movie_booking.service.UserService;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ModelMapper modelMapper;

    /**
     * Returns all currently active shows grouped by movie.
     * Used for homepage now playing section.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SearchResponse> getNowPlaying() {
        log.info("Fetching now playing shows");

        List<Show> shows = showRepository.findNowPlayingShows();

        if (shows.isEmpty()) return List.of();

        return shows.stream()
                .collect(Collectors.groupingBy(show -> show.getMovie().getId()))
                .entrySet().stream()
                .map(entry -> {
                    Show firstShow = entry.getValue().get(0);
                    List<ShowResponse> showResponses = entry.getValue().stream()
                            .map(this::mapToShowResponse)
                            .collect(Collectors.toList());
                    return SearchResponse.builder()
                            .movieId(firstShow.getMovie().getId())
                            .movieTitle(firstShow.getMovie().getTitle())
                            .language(firstShow.getMovie().getLanguage())
                            .genre(firstShow.getMovie().getGenre())
                            .duration(firstShow.getMovie().getDuration())
                            .posterUrl(firstShow.getMovie().getPosterUrl())
                            .shows(showResponses)
                            .build();
                })
                .collect(Collectors.toList());
    }
    /**
     * Searches movies by city and title.
     * Groups results by movie — each movie shows all its available shows in that city.
     */
    @Override
    public List<SearchResponse> searchMovies(String city, String title) {
        log.info("Searching movies with title: '{}' in city: '{}'", title, city);

        List<Show> shows = showRepository.findByMovieTitleAndCity(title, city);

        if (shows.isEmpty()) {
            log.info("No shows found for title: '{}' in city: '{}'", title, city);
            return List.of();
        }

        // Group shows by movie
        return shows.stream()
                .collect(Collectors.groupingBy(show -> show.getMovie().getId()))
                .entrySet().stream()
                .map(entry -> {
                    Show firstShow = entry.getValue().get(0);

                    List<ShowResponse> showResponses = entry.getValue().stream()
                            .map(this::mapToShowResponse)
                            .collect(Collectors.toList());

                    return SearchResponse.builder()
                            .movieId(firstShow.getMovie().getId())
                            .movieTitle(firstShow.getMovie().getTitle())
                            .language(firstShow.getMovie().getLanguage())
                            .genre(firstShow.getMovie().getGenre())
                            .duration(firstShow.getMovie().getDuration())
                            .posterUrl(firstShow.getMovie().getPosterUrl())
                            .shows(showResponses)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns the live seat map for a show.
     * Each seat shows its current status: AVAILABLE, LOCKED, or BOOKED.
     */
    @Override
    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long showId) {
        log.info("Fetching seat map for show id: {}", showId);

        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);

        if (showSeats.isEmpty()) {
            throw new ResourceNotFoundException("No seats found for show id: " + showId);
        }

        Show show = showSeats.get(0).getShow();

        List<SeatMapResponse.SeatInfo> seatInfos = showSeats.stream()
                .map(showSeat -> SeatMapResponse.SeatInfo.builder()
                        .showSeatId(showSeat.getId())
                        .seatCode(showSeat.getSeatLayout().getSeatCode())
                        .rowLabel(showSeat.getSeatLayout().getRowLabel())
                        .seatNumber(showSeat.getSeatLayout().getSeatNumber())
                        .seatType(showSeat.getSeatLayout().getSeatType())
                        .price(showSeat.getSeatLayout().getPrice())
                        .seatStatus(showSeat.getSeatStatus())
                        .build())
                .sorted((a, b) -> {
                    // Sort by rowLabel first, then seatNumber
                    int rowCompare = a.getRowLabel().compareTo(b.getRowLabel());
                    return rowCompare != 0 ? rowCompare :
                            Integer.compare(a.getSeatNumber(), b.getSeatNumber());
                })
                .collect(Collectors.toList());

        log.info("Seat map fetched - {} seats for show: {}", seatInfos.size(), showId);

        return SeatMapResponse.builder()
                .showId(showId)
                .movieTitle(show.getMovie().getTitle())
                .theatreName(show.getScreen().getTheatre().getName())
                .screenName(show.getScreen().getName())
                .seats(seatInfos)
                .build();
    }

    // ─── Mapper ───────────────────────────────────────────────────

    private ShowResponse mapToShowResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .movieId(show.getMovie().getId())
                .movieTitle(show.getMovie().getTitle())
                .screenId(show.getScreen().getId())
                .screenName(show.getScreen().getName())
                .theatreName(show.getScreen().getTheatre().getName())
                .showDate(show.getShowDate())
                .showTime(show.getShowTime())
                .active(show.isActive())
                .build();
    }
}