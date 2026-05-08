package com.bookmyshow.movie_booking.service;

import com.bookmyshow.movie_booking.config.KafkaConfig;
import com.bookmyshow.movie_booking.dto.BookingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingEventProducer {

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingConfirmed(BookingEvent event) {
        if (kafkaTemplate == null) { log.warn("Kafka not available, skipping event"); return; }
        kafkaTemplate.send(KafkaConfig.BOOKING_CONFIRMED_TOPIC, event.getReferenceId(), event);
    }

    public void publishBookingCancelled(BookingEvent event) {
        if (kafkaTemplate == null) { log.warn("Kafka not available, skipping event"); return; }
        kafkaTemplate.send(KafkaConfig.BOOKING_CANCELLED_TOPIC, event.getReferenceId(), event);
    }

    public void publishBookingExpired(BookingEvent event) {
        if (kafkaTemplate == null) { log.warn("Kafka not available, skipping event"); return; }
        kafkaTemplate.send(KafkaConfig.BOOKING_EXPIRED_TOPIC, event.getReferenceId(), event);
    }
}