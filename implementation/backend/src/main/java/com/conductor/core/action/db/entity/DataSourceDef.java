package com.conductor.core.action.db.entity;


import com.conductor.core.action.db.enums.DatasourceType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "datasource", schema = "core")
@Entity
public class DataSourceDef {
    @Id
    private String name;
    private String url;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private DatasourceType type;
}
