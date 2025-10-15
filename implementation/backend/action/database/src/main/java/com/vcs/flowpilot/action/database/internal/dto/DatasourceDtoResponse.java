package com.vcs.flowpilot.action.database.internal.dto;

import com.vcs.flowpilot.action.database.internal.enums.DatasourceStatus;
import com.vcs.flowpilot.action.database.internal.enums.DatasourceType;

import java.time.LocalDateTime;

public record DatasourceDtoResponse(
        String name,
        DatasourceStatus status,
        DatasourceType type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String schema
) {
    @Override
    public String toString() {
        return "DatasourceDtoResponse{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", schema='" + schema + '\'' +
                '}';
    }
}
