package com.vodafone.vcs.conductorwrapper.conductor.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
