package com.conductor.core.action.db.repository;

import com.conductor.core.action.db.entity.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryRepository extends JpaRepository<Query, String> { }
