package com.vodafone.vcs.conductorwrapper.action.database.dto;

import com.vodafone.vcs.conductorwrapper.action.database.enums.QueryType;
import jakarta.validation.constraints.Min;
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
public class QueryStoreDto {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Sql query is required")
    private String sqlQuery;
    @Min(0) @Builder.Default
    private Integer timeoutSeconds = 3;
    @NotBlank(message = "Datasource is required")
    private String dataSource;
    @NotNull(message = "Query type is required")
    private QueryType queryType;
    private Instant createdAt;
    private Instant updatedAt;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", sqlQuery='" + sqlQuery + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                ", dataSource='" + dataSource + '\'' +
                ", queryType=" + queryType +
                '}';
    }
}
