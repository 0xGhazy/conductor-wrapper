package com.vcs.flowpilot.action.http.service;

import com.vcs.flowpilot.action.http.api.AuthStrategyManager;
import com.vcs.flowpilot.action.http.api.XHttpRequest;
import com.vcs.flowpilot.action.http.enums.XGrantType;
import com.vcs.flowpilot.action.http.enums.XHttpMethod;
import com.vcs.flowpilot.action.http.security.OAuth2;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class TokenService {

    private final WebClient webClient;
    private final AuthStrategyManager authStrategyManager;

    @PostConstruct
    @Scheduled(initialDelay = 0, fixedDelay = 120000)
    public Map<String, Object> grapAccessToken() throws InterruptedException {
        OAuth2 strategy = OAuth2
                .builder()
                .webClient(webClient)
                .grantType(XGrantType.PASSWORD)
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
                .build();
        log.info("Test connection: {}", strategy.testAuthentication());

        authStrategyManager.registerAuthConnection("keycloak-cli", strategy, 600);

        XHttpRequest req = new XHttpRequest.builder()
                .url("http://localhost:5050/admin/realms")
                .method(XHttpMethod.GET)
                .connection("keycloak-cli")
                .authStrategyManager(authStrategyManager)
                .build();
        log.info("R1: \n\n {}", req.execute(webClient));

        return Map.of();
    }
}
