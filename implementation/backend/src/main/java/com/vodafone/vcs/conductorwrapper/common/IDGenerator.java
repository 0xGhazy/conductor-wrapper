package com.vodafone.vcs.conductorwrapper.common;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IDGenerator {
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
