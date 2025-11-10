package com.vodafone.vcs.conductorwrapper.action.database.service;

import com.vodafone.vcs.conductorwrapper.action.database.adapter.DatasourceAdapter;
import com.vodafone.vcs.conductorwrapper.action.database.adapter.QueryStoreAdapter;
import com.vodafone.vcs.conductorwrapper.action.database.dto.*;
import com.vodafone.vcs.conductorwrapper.action.database.entity.DataSourceDef;
import com.vodafone.vcs.conductorwrapper.action.database.entity.QueryStore;
import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceStatus;
import com.vodafone.vcs.conductorwrapper.action.database.enums.ErrorCode;
import com.vodafone.vcs.conductorwrapper.action.database.enums.QueryExecStatus;
import com.vodafone.vcs.conductorwrapper.action.database.enums.QueryType;
import com.vodafone.vcs.conductorwrapper.action.database.exception.*;
import com.vodafone.vcs.conductorwrapper.action.database.repository.DatasourceRepository;
import com.vodafone.vcs.conductorwrapper.action.database.repository.QueryStoreRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.sql.SQLTimeoutException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Log4j2
@Service
@AllArgsConstructor
public class DatabaseService {

    private final DatasourceAdapter datasourceAdapter;
    protected final QueryStoreAdapter queryStoreAdapter;
    private final QueryStoreRepository queryStoreRepository;
    private final DatasourceRepository datasourceRepository;
    private final ConcurrentHashMap<String, QueryStore> queriesCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, DatasourceState> states = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        log.info("Initializing database service cache");

        List<QueryStore> queryStoreList = queryStoreRepository.findAll();
        log.debug("Queries fetched successfully, count={}", queryStoreList.size());

        // Load queries to cache
        for (QueryStore query: queryStoreList) {
            String qn = query.getName();
            queriesCache.put(qn, query);
            log.debug("Add [{}] query to cache", qn);
        }
        log.info("Queries are loaded to cache successfully - size={}", queriesCache.size());

        // Load data sources to cache
        Set<String> loadedNames = new HashSet<>();
        List<DataSourceDef> datasourceList = datasourceRepository.findAll();
        log.info("Data sources loaded successfully, count={}", datasourceList.size());

        for (DataSourceDef ds : datasourceList) {
            String name = ds.getName().toUpperCase();
            loadedNames.add(name);
            log.debug("Attempting to register/load '{}' datasource", name);
            try {
                registerDatasource(ds);
            } catch (Exception ex) {
                log.warn("Skip datasource registration for [{}], error occur", name, ex);
            }
        }

        // Pools replacement and closing old opened connections.
        Set<String> obsolete = new HashSet<>(pools.keySet());
        obsolete.removeAll(loadedNames);
        for (String name : obsolete) {
            HikariDataSource old = pools.remove(name);
            templates.remove(name);
            states.computeIfAbsent(name, DatasourceState::new).inactive();
            if (old != null) {
                try { old.close(); }
                catch (Exception e) { throw new GenericErrorException("Failed closing " + name, e); }
            }
        }
        log.info("Database service initialized successfully with pools={}", pools.size());
    }


    @Transactional
    public QueryResult run(@NotNull(message = "Query object is required for execution") Query query) {
        String queryId = query.getId();
        log.info("Attempting to execute query: {}", queryId);

        QueryResult out = QueryResult.builder().queryId(queryId).status(QueryExecStatus.FAILED).build();
        QueryStore fetchedQuery = queriesCache.get(queryId);

        if (fetchedQuery == null) {
            log.debug("Query {} not found in cache - expected QueryStore but got null", queryId);
            log.debug("Respond with error code 'QUERY_NOT_FOUND'");
            out.setErrorCode(ErrorCode.QUERY_NOT_FOUND);
            return out;
        }

        log.debug("Query {} fetched successfully, datasource={}, type={}, timeout={}S",
                queryId, fetchedQuery.getDataSource(), fetchedQuery.getQueryType(), fetchedQuery.getTimeoutSeconds());

        Map<String, Object> params = query.getParams();
        QueryType queryType = fetchedQuery.getQueryType();
        String datasource = fetchedQuery.getDataSource();
        NamedParameterJdbcTemplate template = getJdbcTemplate(datasource);
        log.debug("Datasource JDBC template acquired successfully");

        String sql = fetchedQuery.getSqlQuery().trim();
        log.info(sql);

        // apply per-call timeout (seconds)
        int prevTimeout = template.getJdbcTemplate().getQueryTimeout();
        int timeoutSec = fetchedQuery.getTimeoutSeconds();
        if (timeoutSec > 0) {
            template.getJdbcTemplate().setQueryTimeout(timeoutSec);
            log.info("Timeout set to: {}", template.getJdbcTemplate().getQueryTimeout());
            out.setTimeout(timeoutSec);
        }

        String queryContext = String.format("queryId=%s datasource=%s params=%s", queryId, datasource, params);
        boolean errorOccurFlag = true;

        long start = System.currentTimeMillis();
        try {
            if (QueryType.SELECT.equals(queryType)) {
                var rs = template.queryForList(sql, params);
                out.setResultSet(rs);
                out.setSize(rs.size());
            }
            else {
                int affected = template.update(sql, new MapSqlParameterSource(params));
                out.setAffectedRowsCount(affected);
            }
            out.setStatus(QueryExecStatus.SUCCESS);
            errorOccurFlag = false;
            return out;
        }
        // TODO: Need more investigation: the query timeout and cached successfully, but request timeout.
        catch (QueryTimeoutException e) {
            log.error("Query timeout on queryId={} datasource={}, params={}. Execution exceeded {} seconds.",
                    queryId, datasource, params, timeoutSec);
            out.setErrorCode(ErrorCode.QUERY_TIMEOUT);
            return out;
        }
        catch (CannotGetJdbcConnectionException | TransientDataAccessResourceException e) {
            log.error("Application cannot establish or maintain a connection with datasource: {}", datasource, e);
            out.setErrorCode(ErrorCode.CONNECTION_FAILURE);
            return out;
        }
        catch (DuplicateKeyException e) {
            log.error("Duplicate key violation on {}. Insert/update attempted to insert a primary key or unique constraint value that already exists.", queryContext, e);
            out.setErrorCode(ErrorCode.DUPLICATE_KEY_VIOLATION);
            return out;
        }
        catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation on {}. Insert/update operation violated a database constraint (e.g., foreign key, NOT NULL, or check constraint).", queryContext, e);
            out.setErrorCode(ErrorCode.INTEGRITY_VIOLATION);
            return out;
        }
        catch (BadSqlGrammarException e) {
            log.error("SQL syntax error on {}. Invalid SQL statement or object reference.", queryContext, e);
            out.setErrorCode(ErrorCode.SYNTAX_ERROR);
            return out;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            log.error(e);
            log.error("Result size mismatch on {}. Query returned an unexpected number of rows.", queryContext, e);
            out.setErrorCode(ErrorCode.RESULT_SIZE_MISMATCH);
            return out;
        }
        catch (PermissionDeniedDataAccessException e) {
            log.error("Permission denied on {}. Database user lacks required privileges.", queryContext, e);
            out.setErrorCode(ErrorCode.PERMISSION_DENIED);
            return out;
        }
        catch (UncategorizedDataAccessException e) {
            if (e.getCause() instanceof SQLTimeoutException) {
                log.error("SQL timeout detected on queryId={} datasource={} params={}.",
                        queryId, datasource, params, e);
                out.setErrorCode(ErrorCode.QUERY_TIMEOUT);
            } else {
                log.error("Uncategorized data access error ...", e);
                out.setErrorCode(ErrorCode.UNCLASSIFIED_DAO);
            }
            return out;
        }
        catch (DataAccessException e) {
            log.error("Generic data access error on {}.", queryContext, e);
            out.setErrorCode(ErrorCode.DATA_ACCESS_ERROR);
            return out;
        }
        finally {
            if (timeoutSec > 0) {
                if (prevTimeout <= 0)
                    template.getJdbcTemplate().setQueryTimeout(-1);
                else
                    template.getJdbcTemplate().setQueryTimeout(prevTimeout);
            }
            if (!errorOccurFlag){
                long elapsedMs = (System.currentTimeMillis() - start);
                log.info("Query {} executed in {} ms", queryId, elapsedMs);
            }
        }
    }


    @Transactional
    public void saveAndRegisterDatasource(DataSourceDef payload) {
        Optional<DataSourceDef> dataSourceOpt = datasourceRepository.findByName(payload.getName().toUpperCase());
        if (dataSourceOpt.isPresent())
            throw new DatasourceDuplicationException("Datasource already exists");

        payload.setCreatedAt(Instant.now());
        payload.setName(payload.getName().toUpperCase());
        payload = datasourceRepository.save(payload);
        registerDatasource(payload);
    }

    public Page<DatasourceDtoResponse> listAllDatasources(Pageable pageable) {
        Page<DataSourceDef> page = datasourceRepository.findAll(pageable);

        return page.map(def -> {
            DatasourceStatus status = getState(def.getName().toUpperCase())
                    .map(DatasourceState::getStatus)
                    .orElse(DatasourceStatus.INACTIVE);
            return new DatasourceDtoResponse(
                    def.getName(), status, def.getType(), def.getCreatedAt(), def.getUpdatedAt()
            );
        });
    }

    public DatasourceDtoResponse updateDatasourceByName(
            @NotBlank(message = "Datasource name is required for update") String name,
            @NotNull @Valid DatasourceDto payload) {

        String incomingName = name.toUpperCase();
        String payloadName = payload.getName().toUpperCase();

        if (!incomingName.equals(payloadName))
            throw new GenericErrorException("Request data is inconsistent");

        DataSourceDef ds = datasourceRepository.findByName(payloadName)
                .orElseThrow(() -> new DatasourceNotFoundException("Datasource is not exists"));

        if (hasText(payload.getUrl()))       ds.setUrl(payload.getUrl());
        if (hasText(payload.getUsername()))  ds.setUsername(payload.getUsername());
        if (hasText(payload.getPassword()))  ds.setPassword(payload.getPassword());
        if (hasText(payload.getSchema()))    ds.setSchema(payload.getSchema());

        if (payload.getConnectionTimeout() != null)
            ds.setConnectionTimeout(payload.getConnectionTimeout());

        if (payload.getIdealTimeout() != null)
            ds.setIdealTimeout(payload.getIdealTimeout());

        if (payload.getMaximumPoolSize() != null)
            ds.setMaximumPoolSize(payload.getMaximumPoolSize());

        if (payload.getType() != null)
            ds.setType(payload.getType());

        ds.setUpdatedAt(Instant.now());
        DataSourceDef updated = datasourceRepository.save(ds);

        registerDatasource(updated);
        log.info("Datasource updated successfully");

        return new DatasourceDtoResponse(
                updated.getName(),
                getState(updated.getName().toUpperCase()).get().getStatus(),
                updated.getType(),
                updated.getCreatedAt(),
                updated.getUpdatedAt()
        );
    }

    public DatasourceDto fetchDatasourceBuName(@NotBlank(message = "Datasource name is required for fetching") String name) {
        String datasourceName = name.toUpperCase();
        DataSourceDef ds = datasourceRepository.findByName(datasourceName)
                .orElseThrow(() -> new DatasourceNotFoundException("Datasource is not exists"));

        DatasourceDto result = datasourceAdapter.toDto(ds);
        result.setPassword(null);
        result.setUpdatedAt(ds.getUpdatedAt());
        result.setSchema(ds.getSchema());
        result.setStatus(getState(datasourceName).get().getStatus());
        return result;
    }

    public void deleteDatasourceByName(@NotBlank(message = "Datasource name is required for delete") String name) {
        datasourceRepository.deleteById(name.toUpperCase());
        log.debug("Datasource deleted from database successfully");
        pools.remove(name.toUpperCase());
        log.debug("Datasource deleted from pools successfully");
    }

    @Transactional
    public QueryStoreDto saveAndCacheQuery(QueryStoreDto payload) {
        if (queryStoreRepository.existsByName(payload.getName()))
            throw new QueryAlreadyExistsException("Query is already exists");

        String datasourceName = payload.getDataSource().toUpperCase();
        if (!isValidDatasource(datasourceName))
            throw new DatasourceNotFoundException("Datasource is not exists");

        QueryStore queryStore = queryStoreAdapter.toEntity(payload);
        queryStore.setCreatedAt(Instant.now());

        queryStore = queryStoreRepository.save(queryStore);
        log.debug("Query stored in database successfully");

        queriesCache.put(queryStore.getName(), queryStore);
        log.debug("Query stored in cache successfully");


        return queryStoreAdapter.toDto(queryStore);
    }

    public QueryStoreDto fetchQueryByName(@NotBlank(message = "Query name is required for fetching") String name) {
        QueryStore queryStore = queryStoreRepository.findById(name).orElseThrow(() -> new QueryNotFoundException("Query not found"));
        return  queryStoreAdapter.toDto(queryStore);
    }

    public Page<QueryStore> listAllQueries(Pageable pageable) {
        return queryStoreRepository.findAll(pageable);
    }

    public QueryStoreDto updateQueryByName(String name, QueryStoreDto payload) {
        QueryStore q = queryStoreRepository.findById(name).orElseThrow(() -> new QueryNotFoundException("Query not found"));

        if (!name.equals(payload.getName()))
            throw new GenericErrorException("Request data is inconsistent");

        if (!isValidDatasource(payload.getDataSource().toUpperCase()))
            throw new DatasourceNotFoundException("Datasource is not exists");

        if (payload.getSqlQuery() != null)        q.setSqlQuery(payload.getSqlQuery());
        if (payload.getTimeoutSeconds() != null)  q.setTimeoutSeconds(payload.getTimeoutSeconds());
        if (payload.getDataSource() != null)      q.setDataSource(payload.getDataSource());
        if (payload.getQueryType() != null)       q.setQueryType(payload.getQueryType());

        q.setUpdatedAt(Instant.now());
        q = queryStoreRepository.save(q);
        log.debug("Query {} updated in database", q.getName());

        queriesCache.put(q.getName(), q);
        log.debug("Query {} updated in cache", q.getName());

        return queryStoreAdapter.toDto(q);
    }

    public void deleteQueryByName(@NotBlank(message = "Query name is required for fetching") String name) {
        if (!queryStoreRepository.existsById(name)) throw new QueryNotFoundException("Query is not exists");
        queryStoreRepository.deleteById(name);
        log.debug("Query deleted from database successfully");
        queriesCache.remove(name);
        log.debug("Query deleted from cache successfully");
    }

    // ********** Helper Methods **********

    private NamedParameterJdbcTemplate getJdbcTemplate(String datasource) {
        NamedParameterJdbcTemplate jdbcTemplate = templates.get(datasource.toUpperCase());
        if (jdbcTemplate == null) {
            throw new DatasourceJdbcTemplateNotFound("Unregistered datasource: " + datasource);
        }
        return jdbcTemplate;
    }

    private HikariConfig getHikariConfig(DataSourceDef ds) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(ds.getUrl());
        hc.setUsername(ds.getUsername());
        hc.setPassword(ds.getPassword());

        if (ds.getConnectionTimeout() != null) {
            hc.setConnectionTimeout(ds.getConnectionTimeout());
        }

        if (ds.getMaximumPoolSize() != null) {
            hc.setMaximumPoolSize(ds.getMaximumPoolSize());
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

    public void registerDatasource(DataSourceDef ds) {
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
        } catch (Exception e) {
            if (newPool != null) try { newPool.close(); } catch (Exception ignore) {}

            st.down(e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("password authentication failed")) {
                throw new DatasourceConnectionException("Invalid credentials for '" + name + "'");
            }

            log.error("Register DS {} failed: {}", name, e.toString(), e);
            Throwable c = e.getCause();
            while (c != null) { log.error("Cause: {}", c.toString()); c = c.getCause(); }

            throw new DatasourceRegistrationFailedException("Failed to register: " + name, e);
        }
    }

    private void ping(String name) {
        NamedParameterJdbcTemplate tpl = templates.get(name.toUpperCase());
        DatasourceState st = states.computeIfAbsent(name.toUpperCase(), DatasourceState::new);
        if (tpl == null) { st.inactive(); return; }
        try {
            tpl.getJdbcTemplate().setQueryTimeout(5);
            tpl.getJdbcTemplate().queryForObject("SELECT 1", Integer.class);
            st.ok();
        } catch (Exception ex) {
            st.down(ex.getMessage());
        }
    }

    @Scheduled(fixedDelay = 600000)
    public void periodicHealthCheck() {
        log.debug("Start pinging registered data sources");
        long st = System.currentTimeMillis();
        templates.keySet().forEach(this::ping);
        long duration = System.currentTimeMillis() - st;
        log.debug("Datasource States: {}", getStates());
        log.debug("Pinging finished in {}ms", duration);
    }

    private Map<String, DatasourceState> getStates() {
        return Collections.unmodifiableMap(states);
    }

    private Optional<DatasourceState> getState(String name){
        return Optional.ofNullable(states.get(name.toUpperCase()));
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean isValidDatasource(String datasourceName) {
        boolean existsInPool = (pools.get(datasourceName) != null);
        boolean existsInDatabase = datasourceRepository.existsById(datasourceName);
        log.debug("Datasource: {} exists in pools={}, database={}", datasourceName, existsInPool, existsInDatabase);
        return (existsInPool && existsInDatabase);
    }
}
