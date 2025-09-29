package com.vcs.flowpilot.action.database.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Log4j2
@Component
@RequiredArgsConstructor
public class Query {

    private String id;
    private Map<String, Object> params = new HashMap<>();

    private Query(String name, Map<String, Object> params) {
        this.id = name;
        this.params = params;
    }

    public static class builder {
        private String id;
        private Map<String, Object> p = new HashMap<>();

        public builder id(@NotNull(message = "Query id is required") String id) {
            this.id = id;
            return this;
        }

        public builder withParam(@NotBlank(message = "Parameter name is required") String param,
                                 @NotNull(message = "Parameter value is required") Object value) {
            p.put(param, value);
            return this;
        }

        public builder parameters(Map<String, Object> params) {
            p.putAll(params);
            return this;
        }

        public Query build() {
            if (id == null || id.isEmpty())
                throw new IllegalArgumentException("Query name can not be empty");
            return new Query(id, p);
        }

    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", params=" + params +
                '}';
    }
}

