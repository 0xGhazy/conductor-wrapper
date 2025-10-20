package com.vcs.conductor.exception;

public class DuplicateWorkflowNameException extends RuntimeException {
    public DuplicateWorkflowNameException(String message) {
        super(message);
    }
}
