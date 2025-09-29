package com.vcs.generator;

import com.github.f4b6a3.uuid.UuidCreator;

public class IDGenerator {

    public static String generateTraceIId(String module) {
        return module +  UuidCreator.getTimeOrderedEpoch().toString().replace("-", "");
    }

}
