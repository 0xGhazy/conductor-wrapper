package com.vcs.flowpilot.action.database.internal.exception;

public class QueryNotFoundException extends RuntimeException {
    public QueryNotFoundException(String message) {
        super(message);
    }
}
