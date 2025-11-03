package com.vodafone.vcs.conductor.action.database.service;

import com.vodafone.vcs.conductor.action.database.entity.QueryStore;
import com.vodafone.vcs.conductor.action.database.exception.QueryNotFoundException;
import com.vodafone.vcs.conductor.action.database.repository.QueryStoreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class QueryService {

    private final ConcurrentHashMap<String, QueryStore> queriesCache = new ConcurrentHashMap<>();
    private final QueryStoreRepository queryStoreRepository;

    @PostConstruct
    private void init() {
        log.info("Attempting to load queries to cache");

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
        init();
    }

}
