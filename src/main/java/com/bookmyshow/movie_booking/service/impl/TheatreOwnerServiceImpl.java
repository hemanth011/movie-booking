package com.bookmyshow.movie_booking.service.impl;

import com.bookmyshow.movie_booking.dto.request.ScreenRequest;
import com.bookmyshow.movie_booking.dto.request.SeatLayoutRequest;
import com.bookmyshow.movie_booking.dto.request.ShowRequest;
import com.bookmyshow.movie_booking.dto.request.TheatreRequest;
import com.bookmyshow.movie_booking.dto.response.*;
import com.bookmyshow.movie_booking.entity.*;
import com.bookmyshow.movie_booking.enums.ApprovalStatus;
import com.bookmyshow.movie_booking.enums.SeatStatus;
import com.bookmyshow.movie_booking.exception.ResourceNotFoundException;
import com.bookmyshow.movie_booking.exception.TheatreOwnerNotApprovedException;
import com.bookmyshow.movie_booking.exception.UnauthorizedException;
import com.bookmyshow.movie_booking.repository.*;
import com.bookmyshow.movie_booking.service.TheatreOwnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TheatreOwnerServiceImpl implements TheatreOwnerService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatLayoutRepository seatLayoutRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    // ─── Theatre ──────────────────────────────────────────────────

    /**
     * Adds a new theatre for the logged-in owner.
     * Owner must be APPROVED by admin before adding a theatre.
     */
    @Override
    @Transactional
    public TheatreResponse addTheatre(TheatreRequest request, String ownerEmail) {
        log.info("Theatre owner {} adding new theatre: {}", ownerEmail, request.getName());

        User owner = getApprovedOwner(ownerEmail);

        Theatre theatre = Theatre.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .active(true)
                .owner(owner)
                .build();

        Theatre saved = theatreRepository.save(theatre);
        log.info("Theatre added successfully with id: {}", saved.getId());
        return mapToTheatreResponse(saved);
    }

    /**
     * Updates an existing theatre. Only the owner of the theatre can update it.
     */
    @Override
    @Transactional
    public TheatreResponse updateTheatre(Long theatreId, TheatreRequest request,
                                         String ownerEmail) {
        log.info("Owner {} updating theatre id: {}", ownerEmail, theatreId);

        Theatre theatre = getTheatreOwnedBy(theatreId, ownerEmail);
        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());
        theatre.setCity(request.getCity());
        theatre.setState(request.getState());
        theatre.setPincode(request.getPincode());
        theatre.setPhone(request.getPhone());

        Theatre updated = theatreRepository.save(theatre);
        log.info("Theatre updated: {}", theatreId);
        return mapToTheatreResponse(updated);
    }

    /**
     * Returns all theatres belonging to the logged-in owner.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TheatreResponse> getMyTheatres(String ownerEmail) {
        log.info("Fetching theatres for owner: {}", ownerEmail);
        User owner = getUserByEmail(ownerEmail);
        return theatreRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::mapToTheatreResponse)
                .collect(Collectors.toList());
    }

    // ─── Screen ───────────────────────────────────────────────────

    /**
     * Adds a screen to a theatre. Only the theatre owner can add screens.
     */
    @Override
    @Transactional
    public ScreenResponse addScreen(Long theatreId, ScreenRequest request,
                                    String ownerEmail) {
        log.info("Owner {} adding screen to theatre id: {}", ownerEmail, theatreId);

        Theatre theatre = getTheatreOwnedBy(theatreId, ownerEmail);

        Screen screen = Screen.builder()
                .name(request.getName())
                .totalSeats(request.getTotalSeats())
                .theatre(theatre)
                .build();

        Screen saved = screenRepository.save(screen);
        log.info("Screen added with id: {} to theatre: {}", saved.getId(), theatreId);
        return mapToScreenResponse(saved);
    }

    /**
     * Returns all screens for a given theatre owned by the logged-in owner.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreensByTheatre(Long theatreId, String ownerEmail) {
        log.info("Owner {} fetching screens for theatre: {}", ownerEmail, theatreId);
        getTheatreOwnedBy(theatreId, ownerEmail);
        return screenRepository.findByTheatreId(theatreId)
                .stream()
                .map(this::mapToScreenResponse)
                .collect(Collectors.toList());
    }

    // ─── Seat Layout ──────────────────────────────────────────────

    /**
     * Saves seat layout for a screen. Replaces existing layout.
     * Also regenerates ShowSeat records for all active shows on this screen.
     */
    @Override
    @Transactional
    public List<SeatLayoutResponse> saveSeatLayout(Long screenId,
                                                   List<SeatLayoutRequest> requests,
                                                   String ownerEmail) {
        log.info("Owner {} saving seat layout for screen: {}", ownerEmail, screenId);

        Screen screen = getScreenOwnedBy(screenId, ownerEmail);

        // Delete existing show seats for all shows on this screen
        List<Show> existingShows = showRepository.findByScreenId(screenId);
        existingShows.forEach(show -> {
            showSeatRepository.deleteByShowId(show.getId());
            log.debug("Deleted existing show seats for show: {}", show.getId());
        });

        // Delete existing seat layout
        seatLayoutRepository.deleteByScreenId(screenId);
        log.debug("Existing seat layout deleted for screen: {}", screenId);

        // Save new seat layout
        List<SeatLayout> layouts = requests.stream().map(req -> {
            String seatCode = req.getRowLabel() + req.getSeatNumber();
            return SeatLayout.builder()
                    .rowLabel(req.getRowLabel())
                    .seatNumber(req.getSeatNumber())
                    .seatCode(seatCode)
                    .seatType(req.getSeatType())
                    .price(req.getPrice())
                    .screen(screen)
                    .build();
        }).collect(Collectors.toList());

        List<SeatLayout> saved = seatLayoutRepository.saveAll(layouts);
        log.info("Seat layout saved - {} seats for screen: {}", saved.size(), screenId);

        // Regenerate show seats for all active shows on this screen
        existingShows.stream()
                .filter(Show::isActive)
                .forEach(show -> {
                    generateShowSeats(show, screen, saved);
                    log.info("Regenerated show seats for show: {}", show.getId());
                });

        return saved.stream()
                .map(this::mapToSeatLayoutResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns the seat layout for a given screen.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SeatLayoutResponse> getSeatLayout(Long screenId, String ownerEmail) {
        log.info("Owner {} fetching seat layout for screen: {}", ownerEmail, screenId);
        getScreenOwnedBy(screenId, ownerEmail);
        return seatLayoutRepository.findByScreenId(screenId)
                .stream()
                .map(this::mapToSeatLayoutResponse)
                .collect(Collectors.toList());
    }

    // ─── Show ─────────────────────────────────────────────────────

    /**
     * Adds a show for a screen using a movie from admin's movie list.
     * Auto-generates show seats from the screen's seat layout.
     */
    @Override
    @Transactional
    public ShowResponse addShow(ShowRequest request, String ownerEmail) {
        log.info("Owner {} adding show for movie id: {} on screen id: {}",
                ownerEmail, request.getMovieId(), request.getScreenId());

        Screen screen = getScreenOwnedBy(request.getScreenId(), ownerEmail);

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie", request.getMovieId()));

        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .active(true)
                .build();

        Show saved = showRepository.save(show);
        log.info("Show created with id: {}", saved.getId());

        // Auto-generate show seats from seat layout
        List<SeatLayout> layouts = seatLayoutRepository.findByScreenId(screen.getId());
        generateShowSeats(saved, screen, layouts);

        return mapToShowResponse(saved);
    }

    /**
     * Returns all shows for theatres owned by the logged-in owner.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> getMyShows(String ownerEmail) {
        log.info("Fetching shows for owner: {}", ownerEmail);
        User owner = getUserByEmail(ownerEmail);

        List<Theatre> theatres = theatreRepository.findByOwnerId(owner.getId());

        return theatres.stream()
                .flatMap(theatre -> {
                    List<Screen> screens = screenRepository.findByTheatreId(
                            theatre.getId());
                    return screens.stream()
                            .flatMap(screen ->
                                    showRepository.findByScreenId(screen.getId()).stream()
                            );
                })
                .map(this::mapToShowResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancels a show by setting active = false.
     */
    @Override
    @Transactional
    public void cancelShow(Long showId, String ownerEmail) {
        log.info("Owner {} cancelling show id: {}", ownerEmail, showId);
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));

        String ownerEmailInDb = show.getScreen().getTheatre().getOwner().getEmail();
        if (!ownerEmailInDb.equals(ownerEmail)) {
            throw new UnauthorizedException("You do not own this show");
        }

        show.setActive(false);
        showRepository.save(show);
        log.info("Show cancelled: {}", showId);
    }

    /**
     * Returns all active movies added by admin.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getAvailableMovies() {
        log.info("Fetching available movies for theatre owner");
        return movieRepository.findByActiveTrue()
                .stream()
                .map(this::mapToMovieResponse)
                .collect(Collectors.toList());
    }

    // ─── Private Helpers ──────────────────────────────────────────

    /**
     * Generates ShowSeat entries from given seat layouts.
     * Called when a show is created or seat layout is updated.
     */
    private void generateShowSeats(Show show, Screen screen,
                                   List<SeatLayout> layouts) {
        if (layouts.isEmpty()) {
            log.warn("No seat layout found for screen: {}. " +
                    "Show seats not generated.", screen.getId());
            return;
        }

        List<ShowSeat> showSeats = layouts.stream().map(layout ->
                ShowSeat.builder()
                        .show(show)
                        .seatLayout(layout)
                        .seatStatus(SeatStatus.AVAILABLE)
                        .build()
        ).collect(Collectors.toList());

        showSeatRepository.saveAll(showSeats);
        log.info("Generated {} show seats for show id: {}",
                showSeats.size(), show.getId());
    }

    /**
     * Fetches user by email or throws ResourceNotFoundException.
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    /**
     * Fetches theatre owner and verifies they are APPROVED by admin.
     */
    private User getApprovedOwner(String email) {
        User owner = getUserByEmail(email);
        if (owner.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new TheatreOwnerNotApprovedException();
        }
        return owner;
    }

    /**
     * Fetches a theatre and verifies it belongs to the logged-in owner.
     */
    private Theatre getTheatreOwnedBy(Long theatreId, String ownerEmail) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Theatre", theatreId));
        if (!theatre.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You do not own this theatre");
        }
        return theatre;
    }

    /**
     * Fetches a screen and verifies it belongs to a theatre owned
     * by the logged-in owner.
     */
    private Screen getScreenOwnedBy(Long screenId, String ownerEmail) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Screen", screenId));
        if (!screen.getTheatre().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You do not own this screen");
        }
        return screen;
    }

    // ─── Mappers ──────────────────────────────────────────────────

    private TheatreResponse mapToTheatreResponse(Theatre theatre) {
        return TheatreResponse.builder()
                .id(theatre.getId())
                .name(theatre.getName())
                .address(theatre.getAddress())
                .city(theatre.getCity())
                .state(theatre.getState())
                .pincode(theatre.getPincode())
                .phone(theatre.getPhone())
                .active(theatre.isActive())
                .ownerName(theatre.getOwner().getName())
                .ownerEmail(theatre.getOwner().getEmail())
                .build();
    }

    private ScreenResponse mapToScreenResponse(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .name(screen.getName())
                .totalSeats(screen.getTotalSeats())
                .theatreId(screen.getTheatre().getId())
                .theatreName(screen.getTheatre().getName())
                .build();
    }

    private SeatLayoutResponse mapToSeatLayoutResponse(SeatLayout layout) {
        return SeatLayoutResponse.builder()
                .id(layout.getId())
                .rowLabel(layout.getRowLabel())
                .seatNumber(layout.getSeatNumber())
                .seatCode(layout.getSeatCode())
                .seatType(layout.getSeatType())
                .price(layout.getPrice())
                .screenId(layout.getScreen().getId())
                .build();
    }

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

    private MovieResponse mapToMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .duration(movie.getDuration())
                .cast(movie.getCast())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .active(movie.isActive())
                .build();
    }
}