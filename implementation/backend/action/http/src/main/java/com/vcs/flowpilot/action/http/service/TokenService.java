package com.vcs.flowpilot.action.http.service;

import com.vcs.flowpilot.action.http.api.AuthStrategyManager;
import com.vcs.flowpilot.action.http.api.XHttpRequest;
import com.vcs.flowpilot.action.http.dto.AuthConnection;
import com.vcs.flowpilot.action.http.enums.XGrantType;
import com.vcs.flowpilot.action.http.enums.XHttpMethod;
import com.vcs.flowpilot.action.http.security.OAuth2;
import com.vcs.flowpilot.action.http.security.contract.AuthStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class TokenService {

    private final WebClient webClient;
    private final AuthStrategyManager authStrategyManager;


    @PostConstruct
    public Map<String, Object> grapAccessToken() {

        OAuth2 strategy = OAuth2
                .builder()
                .webClient(webClient)
                .grantType(XGrantType.PASSWORD)
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
                .build();

        authStrategyManager.registerAuthConnection("keycloak-cli", strategy, 600);

        XHttpRequest req = new XHttpRequest.builder()
                .url("http://localhost:5050/admin/realms") // list realms
                .method(XHttpMethod.GET)
                .connection("keycloak-cli")
                .authStrategyManager(authStrategyManager)
                .build();

        String resp = req.execute(webClient);
        System.out.println("RESP1: \n\n " + resp + "\n\n");


        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                String resp2 = req.execute(webClient);
                System.out.println("RESP: " + resp2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 3, TimeUnit.MINUTES);

        return Map.of();
    }
}
