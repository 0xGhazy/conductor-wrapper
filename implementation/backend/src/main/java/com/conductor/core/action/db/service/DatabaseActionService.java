package com.conductor.core.action.db.service;

import com.conductor.core.action.db.config.DataSourceRegistry;
import com.conductor.core.action.db.dto.Query;
import com.conductor.core.action.db.dto.QueryResult;
import com.conductor.core.action.db.entity.DataSourceDef;
import com.conductor.core.action.db.entity.QueryStore;
import com.conductor.core.action.db.enums.QueryType;
import com.conductor.core.action.db.repository.DatasourceRepository;
import com.conductor.core.action.db.repository.QueryStoreRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class DatabaseActionService {

    private final DataSourceRegistry registry;
    private final DatasourceRepository datasourceRepository;
    private final QueryStoreRepository queryStoreRepository;
    private static final Map<String, QueryStore> queries = new HashMap<>();
    private static final Map<String, DataSourceDef> datasourceCache = new HashMap<>();
    @Getter private Map<String, Object> cache = new HashMap<>();

    @PostConstruct
    public Map<String, Object> loadQueries() {
        queries.clear();
        log.debug("Attempting to load queries to cache");
        List<QueryStore> queryStoreList = queryStoreRepository.findAll();
        for (QueryStore q: queryStoreList) {
            queries.put(q.getName(), q);
        }
        log.debug("[{}] Queries are loaded to cache", queries.size());
        cache.put("query", queries);

        datasourceCache.clear();
        log.debug("Attempting to load datasource's to cache");
        List<DataSourceDef> dataSourceDefList = datasourceRepository.findAll();
        for (DataSourceDef d: dataSourceDefList) {
            datasourceCache.put(d.getName(), d);
        }
        log.debug("[{}] Datasource's are loaded to cache", datasourceCache.size());
        cache.put("datasource", datasourceCache);
        return cache;
    }

    public void evictAll() {
        cache = loadQueries();
    }

    @Transactional
    public QueryResult run(Query query) {
        log.info("Attempting to execute query={}", query);
        String queryId = query.getId();
        Map<String, Object> params = query.getParams();

        log.debug("Attempting to fetch query from cache");
        QueryStore fetchedQuery = queryStoreRepository.findById(queryId).orElseThrow( () -> {
            log.error("Query '{}' is not found", queryId);
            throw new IllegalArgumentException("Unknown query=" + queryId);
        });

        QueryType queryType = fetchedQuery.getQueryType();

        var template = registry.template(fetchedQuery.getDataSource().getName());
        log.info("Query datasource loaded successfully");

        String sql = fetchedQuery.getSql().trim().toLowerCase(Locale.ROOT);

        QueryResult queryResult = QueryResult
                .builder()
                .queryId(queryId)
                .params(query.getParams())
                .build();

        List<Map<String, Object>> selectResultSet;
        if (QueryType.SELECT.equals(queryType)) {
            selectResultSet = template.queryForList(sql, params);
            queryResult.setResultSet(selectResultSet);
            queryResult.setSize(selectResultSet.size());
        } else {
            int result = template.update(sql, new MapSqlParameterSource(params));
            queryResult.setAffectedRowsCount(result);
        }
        return queryResult;
    }
}
