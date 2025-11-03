package com.vodafone.vcs.conductor.action.database.repository;

import com.vodafone.vcs.conductor.action.database.entity.DataSourceDef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasourceRepository extends JpaRepository<DataSourceDef, String> {
    Optional<DataSourceDef> findByName(String name);
}
