package com.vodafone.vcs.conductorwrapper.action.database.exception;

public class QueryNotFoundException extends RuntimeException {
    public QueryNotFoundException(String message) {
        super(message);
    }
}
