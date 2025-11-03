package com.vodafone.vcs.conductor.action.database.dto;

import com.vodafone.vcs.conductor.action.database.enums.DatasourceStatus;
import com.vodafone.vcs.conductor.action.database.enums.DatasourceType;

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
