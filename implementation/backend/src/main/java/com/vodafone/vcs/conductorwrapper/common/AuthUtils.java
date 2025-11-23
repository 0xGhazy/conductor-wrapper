package com.vodafone.vcs.conductorwrapper.common;

import java.util.Map;

public class AuthUtils {


    public static Map<String,String> redactHeaders(Map<String,String> in) {
        if (in == null || in.isEmpty()) return Map.of();
        var copy = new java.util.LinkedHashMap<String,String>(in.size());
        in.forEach((k,v) -> {
            String key = k == null ? "" : k;
            if (key.equalsIgnoreCase("authorization") || key.equalsIgnoreCase("cookie")) {
                copy.put(key, "***REDACTED***");
            } else {
                copy.put(key, v);
            }
        });
        return copy;
    }

}
