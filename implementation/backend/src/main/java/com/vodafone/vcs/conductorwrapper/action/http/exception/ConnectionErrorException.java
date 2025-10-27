package com.vodafone.vcs.conductorwrapper.action.http.exception;

public class ConnectionErrorException extends RuntimeException {
    public ConnectionErrorException(String message) {
        super(message);
    }
}
