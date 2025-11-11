package com.vodafone.vcs.conductorwrapper.action.database.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceStatus;
import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatasourceDto {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Url is required")
    private String url;
    @NotBlank(message = "Username is required")
    private String username;
    private String password;
    @Builder.Default
    private Integer connectionTimeout = 3000;
    @Builder.Default
    private Integer idealTimeout = 3000;
    @NotNull(message = "Maximum pool size is required")
    @Builder.Default
    private Integer maximumPoolSize = 5;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Type is required")
    @Builder.Default
    private DatasourceType type = DatasourceType.POSTGRES;
    private String schema;
    @Enumerated(EnumType.STRING)
    private DatasourceStatus status;
    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant updatedAt;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='***** MASKED ****'" +
                ", connectionTimeout=" + connectionTimeout +
                ", idealTimeout=" + idealTimeout +
                ", type=" + type +
                ", schema='" + schema + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}


