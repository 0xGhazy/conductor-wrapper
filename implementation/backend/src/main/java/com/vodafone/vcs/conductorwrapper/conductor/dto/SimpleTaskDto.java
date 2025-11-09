package com.vodafone.vcs.conductorwrapper.conductor.dto;

import lombok.*;

import java.util.Map;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleTaskDto {
    private String name;
    private String refName;
    private String type = "SIMPLE";
    private Map<String, Object> inputParameters;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", refName='" + refName + '\'' +
                ", type='" + type + '\'' +
                ", inputParameters=" + inputParameters +
                '}';
    }
}
