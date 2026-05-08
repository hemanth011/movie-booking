package com.bookmyshow.movie_booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TheatreOwnerNotApprovedException extends RuntimeException {

    public TheatreOwnerNotApprovedException() {
        super("Your account is pending admin approval. You cannot perform this action yet.");
    }
}