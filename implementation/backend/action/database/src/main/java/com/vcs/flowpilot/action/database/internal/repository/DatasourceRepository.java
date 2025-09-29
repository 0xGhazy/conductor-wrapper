package com.vcs.flowpilot.action.database.internal.repository;

import com.vcs.flowpilot.action.database.internal.entity.DataSourceDef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasourceRepository extends JpaRepository<DataSourceDef, String> {
    Optional<DataSourceDef> findByName(String name);
}
