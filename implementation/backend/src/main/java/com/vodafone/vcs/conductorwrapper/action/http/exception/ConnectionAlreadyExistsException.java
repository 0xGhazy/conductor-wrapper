package com.vodafone.vcs.conductorwrapper.action.http.exception;

import com.vodafone.vcs.conductorwrapper.common.exceptions.EntityAlreadyExistsException;
import lombok.Getter;

@Getter
public class ConnectionAlreadyExistsException extends EntityAlreadyExistsException {

    public ConnectionAlreadyExistsException(String message) {
        super(message);
    }
}
