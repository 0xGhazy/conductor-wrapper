package com.vcs.flowpilot.action.http.service;

import com.vcs.flowpilot.action.http.api.XHttpRequest;
import com.vcs.flowpilot.action.http.entity.HttpConnection;
import com.vcs.flowpilot.action.http.enums.AuthenticationStrategy;
import com.vcs.flowpilot.action.http.exception.ConnectionNotFoundException;
import com.vcs.flowpilot.action.http.repository.HttpConnectionsRepository;
import com.vcs.flowpilot.action.http.security.ApiKey;
import com.vcs.flowpilot.action.http.security.OAuth2;
import com.vcs.flowpilot.action.http.security.contract.AuthStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class HttpConnectionService {

    private final WebClient webClient = WebClient.builder().build();
    private final HttpConnectionsRepository repository;
    private final static Map<String, AuthStrategy> connections = new HashMap<>();

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

    public void registerConnection(String name, AuthStrategy strategy) {
        if (strategy instanceof OAuth2)
            connections.put(name, (OAuth2) strategy);
        else if (strategy instanceof ApiKey)
            connections.put(name, (ApiKey) strategy);
    }


    public Map<String, ? extends Serializable> execute(@NotNull XHttpRequest request) {
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
        if (connection != null) req = applyAuthStrategy(connection, req);

        log.info("req: {}", req.retrieve());

        return req.exchangeToMono(resp -> {
                    var status = resp.statusCode().value();
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(bodyStr -> Map.of(
                                    "success", resp.statusCode().is2xxSuccessful(),
                                    "status", status,
                                    "body", bodyStr
                            ));
                })
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.just(Map.of(
                                "success", false,
                                "status", ex.getStatusCode().value(),
                                "body", ex.getResponseBodyAsString()
                        )))
                .onErrorResume(Throwable.class, ex ->
                        Mono.just(Map.of(
                                "success", false,
                                "status", -1,
                                "error", ex.getClass().getSimpleName(),
                                "message", ex.getMessage()
                        )))
                .block();
    }



}
