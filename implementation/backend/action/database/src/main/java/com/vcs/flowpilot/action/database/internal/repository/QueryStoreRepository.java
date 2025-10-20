package com.vcs.flowpilot.action.database.internal.repository;

import com.vcs.flowpilot.action.database.internal.entity.QueryStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryStoreRepository extends JpaRepository<QueryStore, String> { }