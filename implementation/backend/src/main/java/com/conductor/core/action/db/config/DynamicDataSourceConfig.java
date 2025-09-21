package com.conductor.core.action.db.config;

import com.conductor.core.action.db.repository.DatasourceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicDataSourceConfig {

    @Bean
    public DataSourceRegistry dataSourceRegistry(DatasourceRepository repo) {
        DataSourceRegistry reg = new DataSourceRegistry();
        repo.findAll().forEach(reg::createOrUpdate);
        return reg;
    }
}
