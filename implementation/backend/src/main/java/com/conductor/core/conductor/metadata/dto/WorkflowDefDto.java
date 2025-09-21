package com.conductor.core.conductor.metadata.dto;

import com.conductor.core.conductor.metadata.annotation.NoWhitespace;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDefDto {

    @NotBlank(message = "Name is required")
    @NoWhitespace(message = "Name can not contains whitespaces")
    private String name;

    private String description;

    private Long version = 1L;

    @NotNull(message = "Tasks can not be null")
    @NotEmpty(message = "Tasks can not be empty")
    private List<TaskDto> tasks;

    @NotNull(message = "Input parameters can not be null")
    private List<String> inputParameters;

    @NotNull(message = "Output parameters can not be null")
    private Map<String, Object> outputParameters;

    @NotBlank(message = "Owner email is required")
    private String ownerEmail = "conductor.wrapper@vodafone.com";

    @Enumerated(EnumType.STRING)
    private WorkflowDef.TimeoutPolicy timeoutPolicy = WorkflowDef.TimeoutPolicy.ALERT_ONLY;

    private Integer schemaVersion = 2;

    private Long createTime;

    private Long updateTime;

    private String createdBy = "Conductor Wrapper";

    private String updatedBy = "Conductor Wrapper";

    private boolean restartable = true;

    private boolean workflowStatusListenerEnabled = false;

    private Long timeoutSeconds = 0L;

    private Map<String, Object> variables;

    private Map<String, Object> inputTemplate;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", version=" + version +
                ", tasks=" + tasks +
                ", inputParameters=" + inputParameters +
                ", outputParameters=" + outputParameters +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", timeoutPolicy=" + timeoutPolicy +
                ", schemaVersion=" + schemaVersion +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", restartable=" + restartable +
                ", workflowStatusListenerEnabled=" + workflowStatusListenerEnabled +
                ", timeoutSeconds=" + timeoutSeconds +
                ", variables=" + variables +
                ", inputTemplate=" + inputTemplate +
                '}';
    }
}
