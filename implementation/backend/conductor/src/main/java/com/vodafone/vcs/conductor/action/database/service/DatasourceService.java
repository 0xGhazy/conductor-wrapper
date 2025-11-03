package com.vodafone.vcs.conductor.action.database.service;

import com.vodafone.vcs.conductor.action.database.adapter.DatasourceAdapter;
import com.vodafone.vcs.conductor.action.database.dto.DatasourceDto;
import com.vodafone.vcs.conductor.action.database.dto.DatasourceDtoResponse;
import com.vodafone.vcs.conductor.action.database.entity.DataSourceDef;
import com.vodafone.vcs.conductor.action.database.exception.*;
import com.vodafone.vcs.conductor.action.database.repository.DatasourceRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Log4j2
@Component
@AllArgsConstructor
public class DatasourceService {

    private final DatasourceRepository datasourceRepository;
    private final DatasourceAdapter adapter;
    private final ConcurrentMap<String, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        log.info("Init loading data sources");
        List<DataSourceDef> loadedDataSources = datasourceRepository.findAll();
        Set<String> loadedNames = new HashSet<>();

        // register or update each declared datasource
        for (DataSourceDef ds : loadedDataSources) {
            String name = ds.getName().toLowerCase();
            loadedNames.add(name);
            try {
                registerDatasource(ds);
            } catch (Exception ex) {
                log.warn("Skipping datasource [{}] during init: {}", name, ex.getMessage());
            }
        }

        Set<String> existing = new HashSet<>(pools.keySet());
        existing.removeAll(loadedNames);
        for (String obsolete : existing) {
            HikariDataSource removed = pools.remove(obsolete);
            templates.remove(obsolete);
            if (removed != null) {
                try {
                    removed.close();
                    log.info("Closed obsolete pool {}", obsolete);
                } catch (Exception e) {
                    throw new GenericErrorException("Failed closing obsolete pool " + obsolete, e);
                }
            }
        }

        log.info("Data sources init completed. active pools={}", pools.size());
    }

    public NamedParameterJdbcTemplate getJdbcTemplate(String datasource) {
        NamedParameterJdbcTemplate jdbcTemplate = templates.get(datasource);
        if (jdbcTemplate == null) {
            throw new DatasourceJdbcTemplateNotFound("Unregistered datasource: " + datasource);
        }
        return jdbcTemplate;
    }

    public List<DatasourceDtoResponse> cacheRefresh() {
        init();
        return fetchAllDataSources();
    }

    public DatasourceDtoResponse fetchDatasourceByName(String name) {
        DataSourceDef result =  datasourceRepository.findByName(name).orElseThrow(
                () -> new DatasourceNotFoundException("Datasource '" + name + "' is not found"));
        return adapter.toDtoResponse(result, templates);
    }

    public List<DatasourceDtoResponse> fetchAllDataSources() {
        List<DataSourceDef> fetchedAllDataSources = datasourceRepository.findAll();
        List<DatasourceDtoResponse> result = new ArrayList<>();
        for(DataSourceDef ds: fetchedAllDataSources){
            DatasourceDtoResponse responseDto = adapter.toDtoResponse(ds, templates);
            result.add(responseDto);
        }
        return result;
    }

    public DatasourceDtoResponse updateDatasourceByName(String name, DatasourceDto dto) {
        DataSourceDef result =  datasourceRepository.findByName(name).orElseThrow(
                () -> new DatasourceNotFoundException("Datasource '" + name + "' is not found"));

        if (!dto.getName().equals(name)) {
            throw new DatasourceUpdateRequestNameMissMatch("Datasource name in path is not matching name in request body");
        }

        if (dto.getSchema() != null)
            result.setSchema(dto.getSchema());
        if(dto.getType() != null)
            result.setType(dto.getType());
        if(dto.getUrl() != null)
            result.setUrl(dto.getUrl());
        if(dto.getPassword() != null)
            result.setPassword(dto.getPassword());
        if(dto.getUsername() != null)
            result.setUsername(dto.getUsername());
        if(dto.getConnectionTimeout() != null)
            result.setConnectionTimeout(dto.getConnectionTimeout());
        if(dto.getIdealTimeout() != null)
            result.setIdealTimeout(dto.getIdealTimeout());
        result.setUpdatedAt(LocalDateTime.now());

        datasourceRepository.save(result);
        log.info("Datasource {} updated successfully", result);

        registerDatasource(result);

        return adapter.toDtoResponse(result, templates);
    }

    public DatasourceDtoResponse saveAndRegisterDatasource(DatasourceDto dto) {
        String dsName = dto.getName();
        Optional<DataSourceDef> dataSourceOptional = datasourceRepository.findByName(dsName);
        if (dataSourceOptional.isPresent()) {
            throw new DatasourceDuplicationException("Datasource name is already exists");
        }

        DataSourceDef ds = adapter.toEntity(dto);

        // try to register ds
        registerDatasource(ds);

        boolean isActiveDatasource = templates.get(dsName) != null;
        ds.setCreatedAt(LocalDateTime.now());
        datasourceRepository.save(ds);

        return adapter.toDtoResponse(ds, templates);
    }

    //----------------------------- Helpers --------------------------//

    private HikariConfig getHikariConfig(DataSourceDef ds) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(ds.getUrl());
        hc.setUsername(ds.getUsername());
        hc.setPassword(ds.getPassword());
        hc.setMaximumPoolSize(ds.getMaximumPoolSize());

        if (ds.getConnectionTimeout() != null) {
            hc.setConnectionTimeout(ds.getConnectionTimeout());
        }
        if (ds.getIdealTimeout() != null) {
            hc.setIdleTimeout(ds.getIdealTimeout());
        }
        if (ds.getSchema() != null) {
            hc.setSchema(ds.getSchema());
        }

        switch (ds.getType()) {
            case POSTGRES -> hc.setDriverClassName("org.postgresql.Driver");
            case MYSQL    -> hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
            case ORACLE   -> hc.setDriverClassName("oracle.jdbc.OracleDriver");
        }
        return hc;
    }

    private void registerDatasource(DataSourceDef ds) {
        String name  = ds.getName().toLowerCase();
        log.info("Registering datasource {}", name);

        HikariDataSource newPool = null;
        try {
            HikariConfig hc = getHikariConfig(ds);
            newPool = new HikariDataSource(hc);
            NamedParameterJdbcTemplate newTpl = new NamedParameterJdbcTemplate(newPool);

            // returning the old value after putting the new one.
            HikariDataSource oldPool = pools.put(name, newPool);
            templates.put(name, newTpl);

            if (oldPool != null) {
                log.warn("Closing old pool for datasource {}", name);
                try {
                    oldPool.close();
                }
                catch (Exception ex) {
                    log.warn("Failed closing old pool", ex);
                    throw new GenericErrorException("Failed closing old pool", ex);
                }
            }
            log.info("Datasource [{}] registered successfully", name);

        } catch (Exception e) {
            String message = e.getMessage();
            log.error("Failed to register datasource [{}] Cause={}", name, e.getMessage(), e);

            if (message.contains("password authentication failed"))
                throw new DatasourceConnectionException("Invalid credintials for datasource '" + name + "'");

            if (newPool != null) {
                try {
                    newPool.close();
                } catch (Exception ex) {
                    log.warn("Failed closing new pool connection", ex);
                    throw new GenericErrorException("Failed closing new pool connection", ex);
                }
            }

            throw new DatasourceRegistrationFailedException("Failed to register datasource: " + name, e);
        }
    }
}
