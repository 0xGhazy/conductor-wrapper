package com.vodafone.vcs.conductorwrapper.action.http.repository;

import com.vodafone.vcs.conductorwrapper.action.http.entity.HttpConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HttpConnectionsRepository extends JpaRepository<HttpConnection, UUID> {
    boolean existsByNameIgnoreCase(String name);
    Optional<HttpConnection> findByName(String name);
}
