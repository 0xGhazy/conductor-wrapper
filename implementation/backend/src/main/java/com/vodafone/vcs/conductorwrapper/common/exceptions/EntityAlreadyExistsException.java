package com.vodafone.vcs.conductorwrapper.common.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@NoArgsConstructor
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EntityAlreadyExistsException extends RuntimeException{

    private final String reason = "Provided entity already exists";
    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public EntityAlreadyExistsException(String message) {
        super(message);
    }

}
