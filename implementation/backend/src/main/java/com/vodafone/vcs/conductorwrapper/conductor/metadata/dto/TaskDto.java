package com.vodafone.vcs.conductorwrapper.conductor.metadata.dto;

import com.vodafone.vcs.conductorwrapper.conductor.metadata.enums.TaskType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    private String name;
    private String taskReferenceName;
    private Map<String, Object> inputParameters;
    @Enumerated(EnumType.STRING)
    private TaskType type = TaskType.SIMPLE;

    public TaskDto withParameter(@NotBlank(message = "Attribute name is required") String key,
                                 @NotNull(message = "Attribute value can not be null") Object value) {
        inputParameters.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", taskReferenceName='" + taskReferenceName + '\'' +
                ", inputParameters=" + inputParameters +
                ", type=" + type +
                '}';
    }
}
