package com.vcs.flowpilot.action.http.exception;

public class ConnectionErrorException extends RuntimeException {
    public ConnectionErrorException(String message) {
        super(message);
    }
}
