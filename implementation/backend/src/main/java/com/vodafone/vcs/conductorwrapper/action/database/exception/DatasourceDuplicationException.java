package com.vodafone.vcs.conductorwrapper.action.database.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DatasourceDuplicationException extends RuntimeException {

    private final String reason = "Datasource is already exists";
    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public DatasourceDuplicationException(String message) {
        super(message);
    }
}
