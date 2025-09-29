package com.vcs.flowpilot.action.database.internal.exception;

public class DatasourceJdbcTemplateNotFound extends RuntimeException {
    public DatasourceJdbcTemplateNotFound(String message) {
        super(message);
    }
}
