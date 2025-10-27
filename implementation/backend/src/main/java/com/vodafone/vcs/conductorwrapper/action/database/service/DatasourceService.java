package com.vodafone.vcs.conductorwrapper.action.database.service;

import com.vodafone.vcs.conductorwrapper.action.database.adapter.DatasourceAdapter;
import com.vodafone.vcs.conductorwrapper.action.database.dto.DatasourceDto;
import com.vodafone.vcs.conductorwrapper.action.database.dto.DatasourceDtoResponse;
import com.vodafone.vcs.conductorwrapper.action.database.dto.DatasourceState;
import com.vodafone.vcs.conductorwrapper.action.database.entity.DataSourceDef;
import com.vodafone.vcs.conductorwrapper.action.database.exception.*;
import com.vodafone.vcs.conductorwrapper.action.database.repository.DatasourceRepository;
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

    private final DatasourceAdapter adapter;
    private final DatasourceRepository datasourceRepository;
    private final ConcurrentMap<String, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, DatasourceState> states = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        log.info("Init loading data sources");
        List<DataSourceDef> loaded = datasourceRepository.findAll();
        Set<String> loadedNames = new HashSet<>();

        for (DataSourceDef ds : loaded) {
            String name = ds.getName().toUpperCase();
            loadedNames.add(name);
            try { registerDatasource(ds); }
            catch (Exception ex) { log.warn("Skipping [{}]: {}", name, ex.getMessage()); }
        }

        Set<String> obsolete = new HashSet<>(pools.keySet());
        obsolete.removeAll(loadedNames);
        for (String name : obsolete) {
            HikariDataSource old = pools.remove(name);
            templates.remove(name);
            states.computeIfAbsent(name, DatasourceState::new).inactive();
            if (old != null) {
                try { old.close(); }
                catch (Exception e) { throw new GenericErrorException("Failed closing "+name, e); }
            }
        }
        log.info("Init done. pools={}", pools.size());
    }

    public NamedParameterJdbcTemplate getJdbcTemplate(String datasource) {
        NamedParameterJdbcTemplate jdbcTemplate = templates.get(datasource.toUpperCase());
        if (jdbcTemplate == null) {
            throw new DatasourceJdbcTemplateNotFound("Unregistered datasource: " + datasource);
        }
        return jdbcTemplate;
    }

//    public List<DatasourceDtoResponse> cacheRefresh() {
//        init();
//        return fetchAllDataSources();
//    }

//    public DatasourceDtoResponse fetchDatasourceByName(String name) {
//        DataSourceDef result =  datasourceRepository.findByName(name).orElseThrow(
//                () -> new DatasourceNotFoundException("Datasource '" + name + "' is not found"));
//        return adapter.toDtoResponse(result, templates);
//    }

//    public List<DatasourceDtoResponse> fetchAllDataSources() {
//        List<DataSourceDef> fetchedAllDataSources = datasourceRepository.findAll();
//        List<DatasourceDtoResponse> result = new ArrayList<>();
//        for(DataSourceDef ds: fetchedAllDataSources){
//            DatasourceDtoResponse responseDto = adapter.toDtoResponse(ds, templates);
//            result.add(responseDto);
//        }
//        return result;
//    }

//    public DatasourceDtoResponse updateDatasourceByName(String name, DatasourceDto dto) {
//        DataSourceDef result =  datasourceRepository.findByName(name).orElseThrow(
//                () -> new DatasourceNotFoundException("Datasource '" + name + "' is not found"));
//
//        if (!dto.getName().equals(name)) {
//            throw new DatasourceUpdateRequestNameMissMatch("Datasource name in path is not matching name in request body");
//        }
//
//        if (dto.getSchema() != null)
//            result.setSchema(dto.getSchema());
//        if(dto.getType() != null)
//            result.setType(dto.getType());
//        if(dto.getUrl() != null)
//            result.setUrl(dto.getUrl());
//        if(dto.getPassword() != null)
//            result.setPassword(dto.getPassword());
//        if(dto.getUsername() != null)
//            result.setUsername(dto.getUsername());
//        if(dto.getConnectionTimeout() != null)
//            result.setConnectionTimeout(dto.getConnectionTimeout());
//        if(dto.getIdealTimeout() != null)
//            result.setIdealTimeout(dto.getIdealTimeout());
//        result.setUpdatedAt(LocalDateTime.now());
//
//        datasourceRepository.save(result);
//        log.info("Datasource {} updated successfully", result);
//
//        registerDatasource(result);
//
//        return adapter.toDtoResponse(result, templates);
//    }

//    public DatasourceDtoResponse saveAndRegisterDatasource(DatasourceDto dto) {
////        String dsName = dto.getName();
////        Optional<DataSourceDef> dataSourceOptional = datasourceRepository.findByName(dsName);
////        if (dataSourceOptional.isPresent()) {
////            throw new DatasourceDuplicationException("Datasource name is already exists");
////        }
//
//        DataSourceDef ds = adapter.toEntity(dto);
//
//        // try to register ds
//        registerDatasource(ds);
////
////        boolean isActiveDatasource = templates.get(dsName) != null;
////        ds.setCreatedAt(LocalDateTime.now());
////        datasourceRepository.save(ds);
//
//        return adapter.toDtoResponse(ds, templates);
//    }

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
        String name = ds.getName().toUpperCase();
        DatasourceState st = states.computeIfAbsent(name, DatasourceState::new);
        log.info("Registering datasource {}", name);

        HikariDataSource newPool = null;
        try {
            HikariConfig hc = getHikariConfig(ds);
            newPool = new HikariDataSource(hc);
            NamedParameterJdbcTemplate tpl = new NamedParameterJdbcTemplate(newPool);

            HikariDataSource old = pools.put(name, newPool);
            templates.put(name, tpl);
            if (old != null) try { old.close(); } catch (Exception ignore) {}
            log.info("Datasource [{}] registered", name);
        } catch (Exception e) {
            if (newPool != null) try { newPool.close(); } catch (Exception ignore) {}
            st.down(e.getMessage());
            if (e.getMessage()!=null && e.getMessage().contains("password authentication failed"))
                throw new DatasourceConnectionException("Invalid credentials for '"+name+"'");
            throw new DatasourceRegistrationFailedException("Failed to register: "+name, e);
        }
    }

//    public boolean ping(String name) {
//        NamedParameterJdbcTemplate tpl = templates.get(name.toUpperCase());
//        DatasourceState st = states.computeIfAbsent(name.toUpperCase(), DatasourceState::new);
//        if (tpl == null) { st.inactive(); return false; }
//        try {
//            tpl.getJdbcTemplate().setQueryTimeout(5);
//            tpl.getJdbcTemplate().queryForObject("SELECT 1", Integer.class);
//            st.ok();
//            return true;
//        } catch (Exception ex) {
//            st.down(ex.getMessage()); return false;
//        }
//    }
//
//    @Scheduled(fixedDelay = 10000)
//    public void periodicHealthCheck() {
//        log.info("I'm in ping method");
//        templates.keySet().forEach(this::ping);
//    }
//
//    public Map<String, DatasourceState> getStates() { return Collections.unmodifiableMap(states); }
//    public Optional<DatasourceState> getState(String name){ return Optional.ofNullable(states.get(name.toUpperCase())); }
}