package com.vodafone.vcs.conductorwrapper.action.database.entity;

import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceType;
import com.vodafone.vcs.conductorwrapper.common.converters.EncryptionConverter;
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
//    @Convert(converter = EncryptionConverter.class)
    private String url;
//    @Convert(converter = EncryptionConverter.class)
    private String username;
//    @Convert(converter = EncryptionConverter.class)
    private String password;

//    @Convert(converter = EncryptionConverter.class)
    private String schema;
    @Column(name = "connection_timeout")
    private Integer connectionTimeout;
    @Column(name = "ideal_timeout")
    private Integer idealTimeout;
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
