package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.config.KafkaConfig;
import com.bookmyshow.movie_booking.dto.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a booking confirmed event to Kafka.
     * Consumed by notification/analytics services.
     */
    public void publishBookingConfirmed(BookingEvent event) {
        log.info("Publishing booking confirmed event for reference: {}", event.getReferenceId());
        kafkaTemplate.send(KafkaConfig.BOOKING_CONFIRMED_TOPIC, event.getReferenceId(), event);
        log.debug("Booking confirmed event published to topic: {}", KafkaConfig.BOOKING_CONFIRMED_TOPIC);
    }

    /**
     * Publishes a booking cancelled event to Kafka.
     */
    public void publishBookingCancelled(BookingEvent event) {
        log.info("Publishing booking cancelled event for reference: {}", event.getReferenceId());
        kafkaTemplate.send(KafkaConfig.BOOKING_CANCELLED_TOPIC, event.getReferenceId(), event);
    }

    /**
     * Publishes a booking expired event to Kafka.
     */
    public void publishBookingExpired(BookingEvent event) {
        log.info("Publishing booking expired event for reference: {}", event.getReferenceId());
        kafkaTemplate.send(KafkaConfig.BOOKING_EXPIRED_TOPIC, event.getReferenceId(), event);
    }
}