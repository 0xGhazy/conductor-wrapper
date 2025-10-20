package com.vcs.flowpilot.action.database.internal.dto;

import com.vcs.flowpilot.action.database.internal.enums.DatasourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasourceDto {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Url is required")
    private String url;
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Password can not be null")
    private String password;
    @Builder.Default
    private Integer connectionTimeout = 3000;
    @Builder.Default
    private Integer idealTimeout = 3000;
    @NotNull(message = "Type is required")
    private DatasourceType type = DatasourceType.POSTGRES;
    private String schema;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='********** MASKED **********'" +
                ", connectionTimeout=" + connectionTimeout +
                ", idealTimeout=" + idealTimeout +
                ", type=" + type +
                ", schema='" + schema + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}


