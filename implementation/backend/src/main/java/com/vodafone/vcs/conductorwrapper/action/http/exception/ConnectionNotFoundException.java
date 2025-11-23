package com.vodafone.vcs.conductorwrapper.action.http.exception;

import com.vodafone.vcs.conductorwrapper.common.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class ConnectionNotFoundException extends EntityNotFoundException {
    public ConnectionNotFoundException(String message) {
        super(message);
    }
}
