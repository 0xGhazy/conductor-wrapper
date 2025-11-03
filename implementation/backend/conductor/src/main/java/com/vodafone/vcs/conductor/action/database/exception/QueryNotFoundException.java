package com.vodafone.vcs.conductor.action.database.exception;

public class QueryNotFoundException extends RuntimeException {
    public QueryNotFoundException(String message) {
        super(message);
    }
}
