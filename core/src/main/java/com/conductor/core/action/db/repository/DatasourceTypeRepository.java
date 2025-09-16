package com.conductor.core.action.db.repository;

import com.conductor.core.action.db.entity.DatasourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasourceTypeRepository extends JpaRepository<DatasourceType, String> { }
