package com.conductor.core.action.db.entity;

import com.conductor.core.action.db.enums.QueryType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "query_store", schema = "core")
@Entity
public class QueryStore {
    @Id private String name;
    @Column(name="sql_text") private String sql;
    @ManyToOne
    @JoinColumn(name="datasource_name")
    private DataSourceDef dataSource;
    @Column(name = "query_type")
    @Enumerated(EnumType.STRING)
    private QueryType queryType;
}
