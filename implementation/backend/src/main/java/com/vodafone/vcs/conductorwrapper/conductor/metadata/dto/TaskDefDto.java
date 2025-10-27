package com.vodafone.vcs.conductorwrapper.conductor.metadata.dto;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDefDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskDef.TimeoutPolicy timeoutPolicy = TaskDef.TimeoutPolicy.TIME_OUT_WF;

    @Max(value = 10, message = "Retry count range between (0 ~ 10)")
    private Integer retryCount = 3;

    private Long responseTimeoutSeconds = 600L;

    @NotNull(message = "Input keys list can not be null")
    private List<String> inputKeys = new ArrayList<>();


    @NotNull(message = "Output keys list can not be null")
    private List<String> outputKeys = new ArrayList<>();


    @NotBlank(message = "Owner email is required")
    private String ownerEmail = "conductor.wrapper@vodafone.com";

    @Enumerated(EnumType.STRING)
    private TaskDef.RetryLogic retryLogic = TaskDef.RetryLogic.FIXED;

    private Integer retryDelaySeconds = 60;

    private Long timeoutSeconds = 3600L;

    private Integer rateLimitFrequencyInSeconds = 1;

    private Integer rateLimitPerFrequency = 0;

    private Integer setConcurrentExecLimit;

    private Map<String, Object> inputTemplate = new HashMap<>();

    private String createdBy = "Conductor Wrapper";

    private Long createTime = System.currentTimeMillis();

    private String updatedBy = "Conductor Wrapper";

    private Long updateTime = 0L;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", timeoutPolicy=" + timeoutPolicy +
                ", retryCount=" + retryCount +
                ", responseTimeoutSeconds=" + responseTimeoutSeconds +
                ", inputKeys=" + inputKeys +
                ", outputKeys=" + outputKeys +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", retryLogic=" + retryLogic +
                ", retryDelaySeconds=" + retryDelaySeconds +
                ", timeoutSeconds=" + timeoutSeconds +
                ", rateLimitFrequencyInSeconds=" + rateLimitFrequencyInSeconds +
                ", rateLimitPerFrequency=" + rateLimitPerFrequency +
                ", setConcurrentExecLimit=" + setConcurrentExecLimit +
                ", inputTemplate=" + inputTemplate +
                ", createdBy='" + createdBy + '\'' +
                ", createTime=" + createTime +
                ", updatedBy='" + updatedBy + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
