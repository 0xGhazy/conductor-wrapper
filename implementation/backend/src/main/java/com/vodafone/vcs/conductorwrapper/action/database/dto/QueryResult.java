package com.vodafone.vcs.conductorwrapper.action.database.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vodafone.vcs.conductorwrapper.action.database.enums.ErrorCode;
import com.vodafone.vcs.conductorwrapper.action.database.enums.QueryExecStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResult {
    private String queryId;
    private int size;
    private int timeout;
    private QueryExecStatus status;
    private List<Map<String, Object>> resultSet;
    private ErrorCode errorCode;
    private int affectedRowsCount;
    private String traceId;

    @Override
    public String toString() {
        return "{" +
                "queryId='" + queryId + '\'' +
                ", size=" + size +
                ", timeout=" + timeout +
                ", status=" + status +
                ", resultSet=" + resultSet +
                ", errorCode=" + errorCode +
                ", affectedRowsCount=" + affectedRowsCount +
                ", traceId=" + traceId +
                '}';
    }
}