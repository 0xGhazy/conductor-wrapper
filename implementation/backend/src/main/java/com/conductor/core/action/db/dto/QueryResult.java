package com.conductor.core.action.db.dto;

import com.conductor.core.action.db.enums.QueryExecStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class QueryResult {
    private String queryId;
    private Map<String, Object> params;
    private int size;
    private QueryExecStatus status;
    private List<Map<String, Object>> resultSet;
    private int affectedRowsCount;

    @Override
    public String toString() {
        return "{" +
                "queryId='" + queryId + '\'' +
                ", params=" + params +
                ", size=" + size +
                ", status=" + status +
                ", resultSet=" + resultSet +
                ", affectedRowsCount=" + affectedRowsCount +
                '}';
    }
}
