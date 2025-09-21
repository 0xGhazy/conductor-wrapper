package com.conductor.core.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@NoArgsConstructor
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerError extends RuntimeException {

    private final String reason = "System failed to perform requested operation";
    private final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public InternalServerError(String message) {
        super(message);
    }
}