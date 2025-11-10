package com.vodafone.vcs.conductorwrapper.action.database.dto;

import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceStatus;
import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceType;
import lombok.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;

public record DatasourceDtoResponse(
        String name,
        DatasourceStatus status,
        DatasourceType type,
        Instant createdAt,
        Instant updatedAt
) {

    @NonNull @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
