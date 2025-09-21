package com.conductor.core.action.db.repository;

import com.conductor.core.action.db.entity.QueryStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryStoreRepository extends JpaRepository<QueryStore, String> { }
