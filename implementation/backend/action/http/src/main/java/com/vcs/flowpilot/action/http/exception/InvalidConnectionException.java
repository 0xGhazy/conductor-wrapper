package com.vcs.flowpilot.action.http.exception;

public class InvalidConnectionException extends RuntimeException {
    public InvalidConnectionException(String message) {
        super(message);
    }
}
