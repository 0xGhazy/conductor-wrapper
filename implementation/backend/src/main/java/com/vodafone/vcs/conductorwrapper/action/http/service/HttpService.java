package com.vodafone.vcs.conductorwrapper.action.http.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodafone.vcs.conductorwrapper.action.http.adapter.HttpConnectionAdapter;
import com.vodafone.vcs.conductorwrapper.action.http.dto.HTTPConnectionDTO;
import com.vodafone.vcs.conductorwrapper.action.http.dto.ShortDetailedHttpConnectionDTO;
import com.vodafone.vcs.conductorwrapper.action.http.dto.XHttpRequest;
import com.vodafone.vcs.conductorwrapper.action.http.entity.HttpConnection;
import com.vodafone.vcs.conductorwrapper.action.http.enums.AuthenticationStrategy;
import com.vodafone.vcs.conductorwrapper.action.http.exception.ConnectionAlreadyExistsException;
import com.vodafone.vcs.conductorwrapper.action.http.exception.ConnectionNotFoundException;
import com.vodafone.vcs.conductorwrapper.action.http.repository.HttpConnectionsRepository;
import com.vodafone.vcs.conductorwrapper.action.http.security.ApiKey;
import com.vodafone.vcs.conductorwrapper.action.http.security.OAuth2;
import com.vodafone.vcs.conductorwrapper.common.AuthUtils;
import com.vodafone.vcs.conductorwrapper.common.contract.AuthStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
@RequiredArgsConstructor
public class HttpService {

    private final WebClient webClient = WebClient.builder().build();
    private final HttpConnectionsRepository repository;
    private final HttpConnectionAdapter httpConnectionAdapter;
    private final static ConcurrentHashMap<String, AuthStrategy> connectionsPool = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EMPTY_CONNECTION_VALUE = "--NONE--";


    public Map<String, Object> execute(@Valid XHttpRequest request) {
        log.info("Attempting to execute HTTP request from node: {}", request.getNodeName());

        Map<String, Object> response = new HashMap<>();
        Map<String, String> headers = AuthUtils.redactHeaders(request.getHeaders());
        Map<String, String> queries = request.getQueries();
        Object body = request.getBody();
        String connection = request.getConnection();
        String url = buildUrlWithQueryParams(queries, request.getUrl());
        HttpMethod method = request.getMethod();
        log.debug("Request data extracted successfully - method={}, headers={}, connection={}, url={}",
                method, headers, connection, url);

        WebClient.RequestBodySpec spec = webClient
                .method(method)
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // Adding headers to request specs
        if (!headers.isEmpty()) {
            spec = spec.headers(h -> h.setAll(headers));
            log.debug("Request headers added successfully to request specs");
        }

        // Write body payload to the request specs
        WebClient.RequestHeadersSpec<?> req = (body == null ? spec : spec.bodyValue(body));

        boolean applyAuthStrategy = !connection.equalsIgnoreCase(EMPTY_CONNECTION_VALUE);

        // Apply authentication strategy based on passed connection string
        if (applyAuthStrategy) {
            log.debug("Applying {} connection to the request", connection);
            req = applyAuthStrategy(connection, req);
        }

        Map<String, Object> result = req.exchangeToMono(resp -> {
                    int status = resp.statusCode().value();
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(bodyStr -> java.util.Map.of(
                                    "success", resp.statusCode().is2xxSuccessful(),
                                    "status", status,
                                    "body", parseJsonOrReturnString(bodyStr)
                            ));
                })
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.just(java.util.Map.of(
                                "success", false,
                                "status", ex.getStatusCode().value(),
                                "body", parseJsonOrReturnString(ex.getResponseBodyAsString())
                        )))
                .onErrorResume(Throwable.class, ex ->
                        Mono.just(java.util.Map.of(
                                "success", false,
                                "status", -1,
                                "error", ex.getClass().getSimpleName(),
                                "message", ex.getMessage()
                        )))
                .block();

        if (result != null) {
            response.putAll(result);
            response.put("node", request.getNodeName());
            log.debug("Http request execution for {} finished successfully with status code={}",
                    request.getNodeName(), response.getOrDefault("status", "-1"));
        } else {
            log.error("Http request execution for {} failed - response={}", request.getNodeName(), response);
        }

        return response;
    }

    public Set<String> listConnections() {
        log.debug("Attempting to list all connections");
        Set<String> result = connectionsPool.keySet();
        log.debug("Connection loaded successfully, Connection count={}", result.size());
        return result;
    }

    public List<ShortDetailedHttpConnectionDTO> listConnectionsWithStates() {
        List<ShortDetailedHttpConnectionDTO> result = new ArrayList<>();
        List<HttpConnection> connections = repository.findAll();
        for (HttpConnection connection: connections) {
            result.add(ShortDetailedHttpConnectionDTO
                    .builder()
                    .id(connection.getId())
                    .createdAt(Instant.now())
                    .updatedAt(connection.getUpdatedAt())
                    .isActive(connectionsPool.get(connection.getName()).testAuthentication())
                    .strategy(connection.getStrategy())
                    .name(connection.getName())
                    .build());
        }
        return result;
    }

    @Transactional
    public ShortDetailedHttpConnectionDTO registerConnection(HTTPConnectionDTO payload) {
        String name = payload.getName().toUpperCase().trim().replace(" ", "-");

        // Validating connection existence
        boolean alreadyExistsInDatabase = repository.existsByNameIgnoreCase(name);
        boolean alreadyInConnectionPool = (connectionsPool.get(name) != null);
        if (alreadyExistsInDatabase || alreadyInConnectionPool) {
            log.debug("Connection is already exists in, Database={}, Connections Pool={}",
                    alreadyExistsInDatabase, alreadyInConnectionPool);
            throw new ConnectionAlreadyExistsException("Connection is already exists");
        }

        log.debug("Attempting to store connection in database");
        HttpConnection connection = httpConnectionAdapter.toEntity(payload);
        connection.setName(name);
        connection = repository.save(connection);
        log.info("Connection stored successfully in database, id={}", connection.getId());

        registerToConnectionsPool(connection);

        return ShortDetailedHttpConnectionDTO
                .builder()
                .id(connection.getId())
                .createdAt(Instant.now())
                .updatedAt(connection.getUpdatedAt())
                .isActive(connectionsPool.get(name).testAuthentication())
                .strategy(connection.getStrategy())
                .name(name)
                .build();
    }

    private void registerToConnectionsPool(@NotNull(message = "HttpConnection is required") HttpConnection connection) {
        String name = connection.getName();
        log.debug("Attempting to register connection to connection pool");
        AuthenticationStrategy strategy = connection.getStrategy();

        log.debug("Initializing {} instance for connection {}", strategy, name);
        AuthStrategy authStrategy = null;
        if (AuthenticationStrategy.OAUTH2.equals(strategy)) {
            authStrategy = new OAuth2(connection);
        } else if (AuthenticationStrategy.API_KEY.equals(strategy)) {
            authStrategy = new ApiKey(connection);
        }

        if (authStrategy == null) {
            log.error("Failed to initialize the auth strategy - expected to be OAuth|ApiKey but got null");
            log.error("context=[connection={}, pool={}]", connection, connectionsPool);
            return;
        }

        connectionsPool.put(name, authStrategy);
        log.info("Connection {} registered successfully to the connections pool", name);
    }

    public HTTPConnectionDTO fetchConnectionByName(String name) {
        log.debug("Attempting to fetch connection by name {}", name);
        HttpConnection connection = repository.findByName(name).orElseThrow( () -> new ConnectionNotFoundException("Connection not found"));
        log.debug("Connection {} fetched successfully - entity={}", name, connection);
        return httpConnectionAdapter.toDTO(connection);
    }

//    public HTTPConnectionDTO updateConnectionByName(String name, HTTPConnectionDTO payload) {
//
//        // Validate ne
//
//
//    }




    @PostConstruct
    private void init() {
        List<HttpConnection> httpConnectionList = repository.findAll();
        log.info("HTTP connections fetched successfully - count {}", httpConnectionList.size());

        if (!httpConnectionList.isEmpty()) {
            for (HttpConnection con: httpConnectionList) {
                if (con.getStrategy().equals(AuthenticationStrategy.OAUTH2))
                    connectionsPool.put(con.getName(), new OAuth2(con));
                else if (con.getStrategy().equals(AuthenticationStrategy.API_KEY))
                    connectionsPool.put(con.getName(), new ApiKey(con));
            }
            log.info("Connections cached successfully");
            return;
        }

        log.info("No connections found in database");
    }

    private static Object parseJsonOrReturnString(String s) {
        if (s == null || s.isBlank()) return java.util.Map.of();
        try { return MAPPER.readTree(s); }
        catch (Exception ignore) { return s; }
    }

    private AuthStrategy getConnection(String name) {
        AuthStrategy strategy = connectionsPool.get(name);
        if (strategy == null)
            throw new ConnectionNotFoundException("Connection " + name + " not found");
        return strategy;
    }

    private WebClient.RequestHeadersSpec<?> applyAuthStrategy (String connection, WebClient.RequestHeadersSpec<?> req) {
        log.info("Attempting to apply connection : {} on request", connection);
        AuthStrategy strategy = getConnection(connection);

        if (strategy instanceof ApiKey auth) {
            log.info("try to apply API key authentication strategy");
            return auth.apply(req);
        }

        if (strategy instanceof OAuth2 auth) {
            log.info("try to apply OAuth2 authentication strategy");
            return auth.apply(req);
        }

        log.debug("No strategy matched request will be plain without any auth strategy");
        return req;
    }


    private String buildUrlWithQueryParams(Map<String, String> params, String baseUrl) {
        if (params == null || params.isEmpty()) return baseUrl;

        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(baseUrl.contains("?") ? "&" : "?");

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) sb.append("&");
            first = false;
            String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            sb.append(key).append("=").append(value);
        }

        return sb.toString();
    }

    private static int safeBodySize(Map<String, Object> body) {
        if (body == null) return 0;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(body).length;
        } catch (Exception e) {
            return -1;
        }
    }


}
