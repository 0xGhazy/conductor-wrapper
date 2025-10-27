package com.vodafone.vcs.conductorwrapper.action.database.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Getter
@Log4j2
@Builder
public class Query {
    private String id;
    @Singular("withParam")
    private Map<String, Object> params = new HashMap<>();

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", params=" + params +
                '}';
    }
}

