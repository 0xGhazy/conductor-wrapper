package com.vodafone.vcs.conductorwrapper.common;


import java.util.LinkedHashMap;
import java.util.Map;

public class WorkerInputValidator {

    public static String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }

    public static String defaultIfBlank(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    public static Map<String, Object> toStringObjectMap(Object o) {
        if (!(o instanceof Map<?, ?> m)) return Map.of();
        Map<String, Object> out = new LinkedHashMap<>();
        m.forEach((k, v) -> out.put(String.valueOf(k), v));
        return Map.copyOf(out);
    }

    public static Map<String, String> toStringStringMap(Object o) {
        if (!(o instanceof Map<?, ?> m)) return Map.of();
        Map<String, String> out = new LinkedHashMap<>();
        m.forEach((k, v) -> out.put(String.valueOf(k), v == null ? null : String.valueOf(v)));
        return Map.copyOf(out);
    }

}
