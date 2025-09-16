package com.conductor.core.action.db.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "datasource_type", schema = "core")
@Entity
public class DatasourceType {
    private String type;
    @Column(name = "driver_class_name")
    private String driverClassName;
}
