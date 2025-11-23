package com.vodafone.vcs.conductorwrapper.common.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@NoArgsConstructor
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    private final String reason = "Requested entity not found";
    private final HttpStatus status = HttpStatus.NOT_FOUND;

    public EntityNotFoundException(String message) {
        super(message);
    }

}
