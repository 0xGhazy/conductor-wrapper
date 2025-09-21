package com.conductor.core.utils;


import java.util.UUID;

public class IdGenerator {

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
