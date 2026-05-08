package com.bookmyshow.movie_booking.entity;

import com.bookmyshow.movie_booking.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_layouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rowLabel; // A, B, C...

    @Column(nullable = false)
    private Integer seatNumber; // 1, 2, 3...

    @Column(nullable = false)
    private String seatCode; // A1, A2, B1...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType; // SILVER, GOLD, PLATINUM

    @Column(nullable = false)
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;
}