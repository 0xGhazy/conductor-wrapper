package com.conductor.core.action.db.config;

import com.conductor.core.action.db.entity.DataSourceDef;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class DataSourceRegistry {
    private final Map<String, DataSource> pools = new ConcurrentHashMap<>();
    private final Map<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();

    public synchronized void createOrUpdate(DataSourceDef dataSource) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(dataSource.getUrl());
        hc.setUsername(dataSource.getUsername());
        hc.setPassword(dataSource.getPassword());

        switch (dataSource.getType()) {
            case POSTGRES -> hc.setDriverClassName("org.postgresql.Driver");
            case MYSQL    -> hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
            case ORACLE   -> hc.setDriverClassName("oracle.jdbc.OracleDriver");
        }
        HikariDataSource ds = new HikariDataSource(hc);
        pools.put(dataSource.getName(), ds);
        templates.put(dataSource.getName(), new NamedParameterJdbcTemplate(ds));
        log.info("Datasource: {} configured successfully", dataSource.getName());
    }

    public NamedParameterJdbcTemplate template(String dsName) {
        var tpl = templates.get(dsName);
        if (tpl == null) throw new IllegalArgumentException("Unknown datasource: " + dsName);
        return tpl;
    }
}
