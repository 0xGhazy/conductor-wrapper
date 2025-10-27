package com.vodafone.vcs.conductorwrapper.action.database.entity;

import com.vodafone.vcs.conductorwrapper.action.database.enums.QueryType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "query_store", schema = "core")
@Entity
public class QueryStore {
    @Id private String name;

    @Column(name="sql_query")
    private String sqlQuery;

    @Column(name="timeout_seconds")
    private int timeoutSeconds;

    @Column(name="datasource")
    private String dataSource;

    @Column(name = "query_type")
    @Enumerated(EnumType.STRING)
    private QueryType queryType;
}