package com.vcs.flowpilot.action.database.internal.dto;

import com.vcs.flowpilot.action.database.internal.enums.DatasourceStatus;
import com.vcs.flowpilot.action.database.internal.enums.DatasourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDatasourceDto {
    private String name;
    private String url;
    private Integer connectionTimeout;
    private Integer idealTimeout;
    private DatasourceType type;
    private String schema;
    private DatasourceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
