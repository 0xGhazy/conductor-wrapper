package com.vcs.flowpilot.action.http.exception;

public class ConnectionNotFoundException extends RuntimeException {
    public ConnectionNotFoundException(String message) {
        super(message);
    }
}
