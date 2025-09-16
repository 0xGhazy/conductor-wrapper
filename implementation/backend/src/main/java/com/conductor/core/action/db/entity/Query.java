package com.conductor.core.action.db.entity;

import com.conductor.core.action.db.enums.QueryType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Table(name = "queries", schema = "core")
@Entity
public class Query {
    @Id
    private String id;
    private String query;
    @Enumerated(EnumType.STRING)
    @Column(name = "query_type")
    private QueryType queryType;
    private String datasource;
}
