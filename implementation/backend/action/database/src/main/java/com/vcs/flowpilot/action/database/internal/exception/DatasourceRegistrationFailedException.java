package com.vcs.flowpilot.action.database.internal.exception;

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
