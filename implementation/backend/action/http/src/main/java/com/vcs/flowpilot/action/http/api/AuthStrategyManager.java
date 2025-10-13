package com.vcs.flowpilot.action.http.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.vcs.flowpilot.action.http.dto.AuthConnection;
import com.vcs.flowpilot.action.http.exception.ConnectionNotFoundException;
import com.vcs.flowpilot.action.http.security.ApiKey;
import com.vcs.flowpilot.action.http.security.OAuth2;
import com.vcs.flowpilot.action.http.security.contract.AuthStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthStrategyManager {
    private final Cache<String, AuthConnection> connections;

    public void registerAuthConnection(String name, AuthStrategy strategy, long expiresAtEpochSec) {
        if (strategy == null || name == null || name.isBlank()) return;
        connections.put(name, new AuthConnection(strategy, expiresAtEpochSec));
        log.info("Registered connection {} - expire at={}", name, expiresAtEpochSec);
    }

    private AuthConnection getConnection(String connection) {
        AuthConnection authConnection = connections.getIfPresent(connection);
        if (authConnection == null) {
            throw new ConnectionNotFoundException("Connection " + connection + " not found");
        }
        AuthStrategy strategy = authConnection.getStrategy();
        if (strategy instanceof OAuth2 oauth) {
            oauth.refreshIfNeeded();
        }
        return authConnection;
    }

    public WebClient.RequestHeadersSpec<?> applyAuthStrategy (String connection, WebClient.RequestHeadersSpec<?> req) {
        log.info("Attempting to apply connection : {} on request", connection);
        AuthConnection authConnection = getConnection(connection);

        AuthStrategy strategy = authConnection.getStrategy();
        if (strategy instanceof ApiKey auth) {
            log.debug("try to apply API key strategy");
            return  auth.apply(req);
        }

        if (strategy instanceof OAuth2 auth) {
            log.debug("try to apply API key strategy");

            return auth.apply(req);
        }

        log.debug("No strategy matched request will be plain without any auth strategy");
        return req;
    }

}
