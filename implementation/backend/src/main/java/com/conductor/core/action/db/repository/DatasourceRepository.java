package com.conductor.core.action.db.repository;

import com.conductor.core.action.db.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasourceRepository extends JpaRepository<DataSource, String> { }
