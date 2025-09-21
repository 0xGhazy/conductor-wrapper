package com.conductor.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationResponseError {
    private String field;
    private String message;

    @Override
    public String toString() {
        return "{" +
                "field='" + field + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
