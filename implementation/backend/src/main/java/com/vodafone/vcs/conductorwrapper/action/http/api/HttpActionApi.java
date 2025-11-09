package com.vodafone.vcs.conductorwrapper.action.http.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodafone.vcs.conductorwrapper.action.http.dto.XHttpRequest;
import com.vodafone.vcs.conductorwrapper.action.http.entity.HttpConnection;
import com.vodafone.vcs.conductorwrapper.action.http.enums.AuthenticationStrategy;
import com.vodafone.vcs.conductorwrapper.action.http.exception.ConnectionNotFoundException;
import com.vodafone.vcs.conductorwrapper.action.http.repository.HttpConnectionsRepository;
import com.vodafone.vcs.conductorwrapper.action.http.security.ApiKey;
import com.vodafone.vcs.conductorwrapper.action.http.security.OAuth2;
import com.vodafone.vcs.conductorwrapper.common.contract.AuthStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.*;

@Log4j2
@Component
@RequiredArgsConstructor
public class HttpActionApi {

    private final WebClient webClient = WebClient.builder().build();
    private final HttpConnectionsRepository repository;
    private final static Map<String, AuthStrategy> connections = new HashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Set<String> listConnections() {
        return connections.keySet();
    }

    public void registerConnection(String name, AuthStrategy strategy) {
        if (strategy instanceof OAuth2)
            connections.put(name, (OAuth2) strategy);
        else if (strategy instanceof ApiKey)
            connections.put(name, (ApiKey) strategy);
    }

    private static Object parseJsonOrReturnString(String s) {
        if (s == null || s.isBlank()) return java.util.Map.of();
        try { return MAPPER.readTree(s); }
        catch (Exception ignore) { return s; }
    }

    public Map<String, Object> execute(@NotNull XHttpRequest request) {
        Map<String, String> headers = request.getHeaders();
        Object body = request.getBody();
        String connection = request.getConnection();

        WebClient.RequestBodySpec spec = webClient.method(request.getMethod())
                .uri(request.getUrl())
                .accept(MediaType.APPLICATION_JSON);

        if (headers != null && !headers.isEmpty()) {
            spec = spec.headers(h -> h.setAll(headers));
        }

        WebClient.RequestHeadersSpec<?> req = (body == null ? spec : spec.bodyValue(body));
        if (!connection.equals("--NONE--")) {
            req = applyAuthStrategy(connection, req);
            log.info("I am apply the connection string");
        }

        return req.exchangeToMono(resp -> {
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
    }

    @PostConstruct
    private void init() {
        List<HttpConnection> httpConnectionList = repository.findAll();
        log.info("HTTP connections fetched successfully - count {}", httpConnectionList.size());

        if (!httpConnectionList.isEmpty()) {
            for (HttpConnection con: httpConnectionList) {
                if (con.getStrategy().equals(AuthenticationStrategy.OAUTH2))
                    connections.put(con.getName(), new OAuth2(con));
                else if (con.getStrategy().equals(AuthenticationStrategy.API_KEY))
                    connections.put(con.getName(), new ApiKey(con));
            }
            log.info("Connections cached successfully");
            return;
        }

        log.info("No connections found in database");
    }

    private AuthStrategy getConnection(String name) {
        AuthStrategy strategy = connections.get(name);
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

}
