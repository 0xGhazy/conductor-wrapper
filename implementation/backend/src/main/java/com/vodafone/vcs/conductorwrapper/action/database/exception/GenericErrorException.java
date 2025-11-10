package com.vodafone.vcs.conductorwrapper.action.database.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Setter
@Getter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class GenericErrorException extends RuntimeException {

    private Exception parent;
    private final String reason = "Datasource is already exists";
    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public GenericErrorException(String message) {
        super(message);
    }

    public GenericErrorException(String message, Exception e) {
        super(message);
        this.parent = e;
    }
}
