package com.vodafone.vcs.conductorwrapper.action.database.exception;

import lombok.Getter;
import lombok.Setter;

public class DatasourceRegistrationFailedException extends RuntimeException {
    @Getter @Setter
    private Exception parentException;

    public DatasourceRegistrationFailedException(String message) {
        super(message);
    }

    public DatasourceRegistrationFailedException(String message, Exception parent) {
        super(message);
        this.parentException = parent;
    }
}
