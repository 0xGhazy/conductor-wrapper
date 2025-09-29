package com.vcs.flowpilot.action.database.internal.service;

import com.vcs.flowpilot.action.database.internal.entity.QueryStore;
import com.vcs.flowpilot.action.database.internal.exception.QueryNotFoundException;
import com.vcs.flowpilot.action.database.internal.repository.QueryStoreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class QueryService {

    private final static Map<String, QueryStore> queriesCache = new HashMap<>();
    private final QueryStoreRepository queryStoreRepository;

    @PostConstruct
    private void initQueryCache() {
        log.info("Attempting to load queries to cache");
        queriesCache.clear();

        List<QueryStore> queryStoreList = queryStoreRepository.findAll();
        log.debug("Queries fetched successfully, Queries count = {}", queryStoreList.size());

        for (QueryStore query: queryStoreList) {
            String queryName = query.getName();
            queriesCache.put(queryName, query);
        }

        log.info("Queries are loaded successfully");
    }

    public QueryStore readFromCache(String query) {
        QueryStore fq = queriesCache.get(query);
        if (fq == null) {
            log.warn("Query {} is not exist in query cache", query);
            throw new QueryNotFoundException("Query " + query + " not found");
        }
        return fq;
    }

    public void evict() {
        initQueryCache();
    }

}
