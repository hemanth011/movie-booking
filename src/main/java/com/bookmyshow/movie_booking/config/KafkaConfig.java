package com.bookmyshow.movie_booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String BOOKING_CONFIRMED_TOPIC = "booking-confirmed";
    public static final String BOOKING_CANCELLED_TOPIC = "booking-cancelled";
    public static final String BOOKING_EXPIRED_TOPIC = "booking-expired";

    /**
     * Topic for confirmed bookings — used for notifications, analytics.
     */
    @Bean
    public NewTopic bookingConfirmedTopic() {
        return TopicBuilder.name(BOOKING_CONFIRMED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic for cancelled bookings.
     */
    @Bean
    public NewTopic bookingCancelledTopic() {
        return TopicBuilder.name(BOOKING_CANCELLED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic for expired bookings (payment timeout).
     */
    @Bean
    public NewTopic bookingExpiredTopic() {
        return TopicBuilder.name(BOOKING_EXPIRED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}