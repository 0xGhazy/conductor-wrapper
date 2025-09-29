package com.vcs.flowpilot.action.database.internal.service;

import com.vcs.flowpilot.action.database.internal.adapter.DatasourceAdapter;
import com.vcs.flowpilot.action.database.internal.dto.DatasourceDto;
import com.vcs.flowpilot.action.database.internal.dto.ResponseDatasourceDto;
import com.vcs.flowpilot.action.database.internal.entity.DataSourceDef;
import com.vcs.flowpilot.action.database.internal.enums.DatasourceStatus;
import com.vcs.flowpilot.action.database.internal.exception.DatasourceDuplicationException;
import com.vcs.flowpilot.action.database.internal.exception.DatasourceJdbcTemplateNotFound;
import com.vcs.flowpilot.action.database.internal.exception.DatasourceNotFoundException;
import com.vcs.flowpilot.action.database.internal.exception.DatasourceUpdateRequestNameMissMatch;
import com.vcs.flowpilot.action.database.internal.repository.DatasourceRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
@AllArgsConstructor
public class DatasourceService {

    private final DatasourceRepository datasourceRepository;
    private final DatasourceAdapter adapter;
    private static final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();
    private static final Map<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        log.info("Init loading data sources");
        templates.clear();
        List<DataSourceDef> loadedDataSources = datasourceRepository.findAll();
        log.info("Data sources loaded successfully, data sources count = {}", loadedDataSources.size());

        for (DataSourceDef ds: loadedDataSources) {
            registerDatasource(ds);
        }

        log.info("Data sources registration completed successfully");
    }

    public NamedParameterJdbcTemplate getJdbcTemplate(String datasource) {
        NamedParameterJdbcTemplate jdbcTemplate = templates.get(datasource);
        if (jdbcTemplate == null) {
            throw new DatasourceJdbcTemplateNotFound("Unregistered datasource: " + datasource);
        }
        return jdbcTemplate;
    }

    public List<ResponseDatasourceDto> cacheRefresh() {
        init();
        return fetchAllDataSources();
    }

    public DataSourceDef registerDatasource(DataSourceDef ds) {
        String datasourceName = ds.getName();
        log.info("Attempting to register datasource : {}", datasourceName);
        try {

            HikariDataSource oldPool = pools.remove(datasourceName);
            if (oldPool != null) {
                log.warn("Closing old pool for datasource {}", datasourceName);
                oldPool.close();
            }

            HikariConfig hc = getHikariConfig(ds);
            log.debug("Datasource [{}] configuration prepared", datasourceName);

            HikariDataSource hds = new HikariDataSource(hc);

            templates.put(datasourceName, new NamedParameterJdbcTemplate(hds));
            log.info("Datasource [{}] registered successfully", datasourceName);
        } catch (Exception e) {
            String message = e.getMessage();
            log.error("Failed to register datasource [{}] with URL={} user={}. Cause={}",
                    datasourceName, ds.getUrl(), ds.getUsername(), e.getMessage(), e);
            if (message.contains("password authentication failed")) {
                log.error("Invalid credintials for datasource [{}]", datasourceName);
            }
        }
        return ds;
    }

    public ResponseDatasourceDto fetchDatasourceByName(String name) {
        DataSourceDef result =  datasourceRepository.findByName(name).orElseThrow(
                () -> new DatasourceNotFoundException("Datasource '" + name + "' is not found"));
        ResponseDatasourceDto responseDto = adapter.toResponseDto(result);
        responseDto.setStatus( templates.get(responseDto.getName()) != null ? DatasourceStatus.ACTIVE: DatasourceStatus.INACTIVE );
        return responseDto;
    }

    public List<ResponseDatasourceDto> fetchAllDataSources() {
        List<DataSourceDef> fetchedAllDataSources = datasourceRepository.findAll();
        List<ResponseDatasourceDto> result = new ArrayList<>();

        for(DataSourceDef ds: fetchedAllDataSources){
            ResponseDatasourceDto responseDto = adapter.toResponseDto(ds);
            responseDto.setStatus( templates.get(responseDto.getName()) != null ? DatasourceStatus.ACTIVE: DatasourceStatus.INACTIVE );
            result.add(responseDto);
        }
        return result;
    }

    public ResponseDatasourceDto updateDatasourceByName(String name, DatasourceDto dto) {
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

        result = registerDatasource(result);

        ResponseDatasourceDto resultWithStatus = adapter.toResponseDto(result);
        resultWithStatus.setStatus( templates.get(result.getName()) != null ? DatasourceStatus.ACTIVE : DatasourceStatus.INACTIVE);

        return resultWithStatus;
    }

    public ResponseDatasourceDto saveAndRegisterDatasource(DatasourceDto dto) {
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

        ResponseDatasourceDto result = adapter.toResponseDto(ds);
        result.setStatus(isActiveDatasource ? DatasourceStatus.ACTIVE : DatasourceStatus.INACTIVE);
        return result;
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
}
