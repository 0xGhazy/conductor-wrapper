package com.vodafone.vcs.conductorwrapper.action.database.exception;

public class DatasourceNotFoundException extends RuntimeException {
    public DatasourceNotFoundException(String message) {
        super(message);
    }
}
