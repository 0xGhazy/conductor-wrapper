package com.conductor.core.action.db.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "datasources", schema = "core")
@Entity
public class DataSource {
    private String name;
    private String url;
    private String password;
    // TODO: make relation here
    private DatasourceType type;
}
