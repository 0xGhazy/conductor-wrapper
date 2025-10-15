package com.vcs.flowpilot.action.database.internal.dto;

import com.vcs.flowpilot.action.database.internal.enums.ErrorCode;
import com.vcs.flowpilot.action.database.internal.enums.QueryExecStatus;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@Builder
@ToString
public class QueryResult {
    private String queryId;
    private int size;
    private int timeout;
    private QueryExecStatus status;
    private List<Map<String, Object>> resultSet;
    private ErrorCode errorCode;
    private int affectedRowsCount;
}