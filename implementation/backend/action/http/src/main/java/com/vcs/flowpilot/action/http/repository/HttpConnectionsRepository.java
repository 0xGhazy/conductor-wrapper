package com.vcs.flowpilot.action.http.repository;

import com.vcs.flowpilot.action.http.entity.HttpConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HttpConnectionsRepository extends JpaRepository<HttpConnection, UUID> { }
