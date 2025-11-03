package com.vodafone.vcs.conductor.action.database.exception;

public class DatasourceNotFoundException extends RuntimeException {
    public DatasourceNotFoundException(String message) {
        super(message);
    }
}
