package com.vodafone.vcs.conductorwrapper.action.database.repository;

import com.vodafone.vcs.conductorwrapper.action.database.entity.QueryStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryStoreRepository extends JpaRepository<QueryStore, String> { }