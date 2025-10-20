package com.vcs.flowpilot.action.database.internal.exception;

public class DatasourceNotFoundException extends RuntimeException {
    public DatasourceNotFoundException(String message) {
        super(message);
    }
}
