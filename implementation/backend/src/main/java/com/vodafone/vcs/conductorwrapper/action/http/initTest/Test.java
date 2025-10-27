package com.vodafone.vcs.conductorwrapper.action.http.initTest;

import com.vodafone.vcs.conductorwrapper.action.http.entity.HttpConnection;
import com.vodafone.vcs.conductorwrapper.action.http.enums.AuthenticationStrategy;
import com.vodafone.vcs.conductorwrapper.action.http.enums.XGrantType;
import com.vodafone.vcs.conductorwrapper.action.http.repository.HttpConnectionsRepository;
import com.vodafone.vcs.conductorwrapper.action.http.security.OAuth2;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Component
@RequiredArgsConstructor
public class Test {

    private final HttpConnectionsRepository repository;

    @PostConstruct
    public void init() {
        OAuth2 strategy = OAuth2
                .builder()
                .webClient(WebClient.builder().build())
                .grantType(XGrantType.PASSWORD)
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
                .build();
        log.info("Test connection: {}", strategy.testAuthentication());

        HttpConnection conn2 = HttpConnection
                .builder()
                .name("keycloak-cli2")
                .clientId("admin-cli")
                .grantType(XGrantType.PASSWORD)
                .username("admin")
                .strategy(AuthenticationStrategy.OAUTH2)
                .password("admin")
                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
                .build();

        HttpConnection conn3 = HttpConnection
                .builder()
                .name("keycloak-cli3")
                .clientId("admin-cli")
                .grantType(XGrantType.PASSWORD)
                .username("admin")
                .strategy(AuthenticationStrategy.OAUTH2)
                .password("admin")
                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
                .build();

        HttpConnection conn4 = HttpConnection
                .builder()
                .name("keycloak-cli4")
                .clientId("admin-cli")
                .grantType(XGrantType.PASSWORD)
                .username("admin")
                .strategy(AuthenticationStrategy.OAUTH2)
                .password("admin")
                .tokenEndpoint("http://localhost:5050/realms/master/protocol/openid-connect/token")
                .build();

    }

}
