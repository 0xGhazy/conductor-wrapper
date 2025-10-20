package com.vcs.flowpilot.action.http.controller;

import com.vcs.flowpilot.action.http.api.XHttpRequest;
import com.vcs.flowpilot.action.http.dto.RequestDto;
import com.vcs.flowpilot.action.http.service.HttpConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@RestController
@RequestMapping("/api/action/http")
@RequiredArgsConstructor
public class HttpController {

    private final HttpConnectionService service;

    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    public String executeRequest(@Valid @RequestBody RequestDto payload) {
        log.info("Payload: {}", payload);

//        log.info("------------------------------------- Start registering connection -------------------------------------");
//        OAuth2 strategy = OAuth2
//                .builder()
//                .webClient(webClient)
//                .grantType(XGrantType.PASSWORD)
//                .clientId("admin-cli")
//                .username("admin")
//                .password("admin")
//                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
//                .build();
//        log.info("Test connection: {}", strategy.testAuthentication());
//
//        HttpConnections conn1 = HttpConnections
//                .builder()
//                .name("keycloak-cli")
//                .clientId("admin-cli")
//                .grantType(XGrantType.PASSWORD)
//                .username("admin")
//                .strategy(AuthenticationStrategy.OAUTH2)
//                .password("admin")
//                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
//                .build();
//        HttpConnections conn2 = HttpConnections
//                .builder()
//                .name("test-api-key")
//                .apiKey("admin-cli-123-456-789")
//                .apiKeyHeader("X-API-KEY")
//                .strategy(AuthenticationStrategy.API_KEY)
//                .build();
//        log.info("Connection stored successfully - {}", repository.save(conn1));
//        log.info("Connection stored successfully - {}", repository.save(conn2));
//
//        authStrategyManager.registerAuthConnection("keycloak-cli", strategy, 600);
//        log.info("------------------------------------- Finish registering connection -------------------------------------");

        XHttpRequest request = new XHttpRequest
                .builder()
                .url(payload.getUrl())
                .headers(payload.getHeaders())
                .body(payload.getBody())
                .connection(payload.getConnection())
                .method(payload.getMethod())
                .build();

        log.info(service.execute(request));

        return "";
    }
}
