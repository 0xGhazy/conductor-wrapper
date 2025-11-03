package com.vodafone.vcs.conductor.action.database.entity;

import com.vodafone.vcs.conductor.action.database.enums.DatasourceType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

@Data
@Table(name = "datasource", schema = "core")
@Entity
public class DataSourceDef {
    @Id
    private String name;

    private String url;

    private String username;

    private String password;

    @Column(name = "connection_timeout")
    private Integer connectionTimeout;

    @Column(name = "ideal_timeout")
    private Integer idealTimeout;

    @Column(name = "\"schema\"")
    private String schema;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private DatasourceType type;

    @Column(name = "maximum_pool_size")
    private Integer maximumPoolSize;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='********** Masked **********'" +
                ", connectionTimeout=" + connectionTimeout +
                ", idealTimeout=" + idealTimeout +
                ", schema='" + schema + '\'' +
                ", type=" + type +
                ", maximumPoolSize=" + maximumPoolSize +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
