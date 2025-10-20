package com.vcs.flowpilot.action.database.internal.exception;

import lombok.Getter;
import lombok.Setter;

public class GenericErrorException extends RuntimeException {

    @Getter @Setter
    private Exception parent;

    public GenericErrorException(String message) {
        super(message);
    }

    public GenericErrorException(String message, Exception e) {
        super(message);
        this.parent = e;
    }
}
