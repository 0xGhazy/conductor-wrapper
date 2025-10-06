package com.vcs.flowpilot.action.http.security;

import com.vcs.flowpilot.action.http.dto.TokenResp;
import com.vcs.flowpilot.action.http.security.contract.AuthStrategy;
import com.vcs.flowpilot.action.http.enums.XGrantType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class OAuth2 implements AuthStrategy {
    private final WebClient webClient;
    private XGrantType grantType;
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private String scope;
    private volatile String accessToken;
    @Builder.Default
    private volatile Instant expiresAt = Instant.EPOCH;
    private String username;
    private String password;
    private String code;
    private String redirectUri;
    private String codeVerifier;
    private BodyInserters.FormInserter<String> form;

    private void buildStrategy() {
        validateRequired();
        switch (Objects.requireNonNull(grantType)) {
            case PASSWORD -> {
                form = BodyInserters
                        .fromFormData("grant_type", "password")
                        .with("client_id", clientId)
                        .with("username", username)
                        .with("password", password);
                addIfPresent(form, "client_secret", clientSecret);
                addIfPresent(form, "scope", scope);
            }
            case CLIENT_CREDENTIALS -> {
                form = BodyInserters
                        .fromFormData("grant_type", "client_credentials")
                        .with("client_id", "admin-cli")
                        .with("client_secret", "admin");
                addIfPresent(form, "scope", scope);
            }
            case AUTHORIZATION_CODE -> {
                form = BodyInserters
                        .fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("code", code)
                        .with("redirect_uri", redirectUri);
                addIfPresent(form, "client_secret", clientSecret);
                addIfPresent(form, "code_verifier", codeVerifier);
            }
        }


        TokenResp t = webClient.post()
                .uri("http://localhost:5050/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .bodyToMono(TokenResp.class)
                .block();
    }

    @Override
    public WebClient.RequestHeadersSpec<?> apply(WebClient.RequestHeadersSpec<?> req) {
        validateRequired();
        String token = getAccessToken();
        return req.headers(h -> h.setBearerAuth(token));
    }

    private void validateRequired() {
        List<String> missing = new ArrayList<>();
        if (blank(tokenEndpoint)) missing.add("tokenEndpoint");
        if (grantType == null)    missing.add("grantType");

        switch (Objects.requireNonNull(grantType)) {
            case CLIENT_CREDENTIALS -> {
                if (blank(clientId))     missing.add("clientId");
                if (blank(clientSecret)) missing.add("clientSecret");
            }
            case PASSWORD -> {
                if (blank(clientId))  missing.add("clientId");
                if (blank(username))  missing.add("username");
                if (blank(password))  missing.add("password");
            }
            case AUTHORIZATION_CODE -> {
                if (blank(clientId))    missing.add("clientId");
                if (blank(code))        missing.add("code");
                if (blank(redirectUri)) missing.add("redirectUri");
            }
        }
        if (!missing.isEmpty())
            throw new IllegalArgumentException("Missing required fields for " + grantType + ": " + missing);
    }

    private String getAccessToken() {
        if (accessToken != null && Instant.now().isBefore(expiresAt.minusSeconds(30))) return accessToken;
        TokenResp tr = fetchToken();
        if (tr == null || tr.accessToken() == null) throw new IllegalStateException("No access_token from token endpoint");
        accessToken = tr.accessToken();
        long ttl = tr.expiresIn() != null ? tr.expiresIn() : 300L;
        expiresAt = Instant.now().plusSeconds(ttl);
        return accessToken;
    }

    public TokenResp fetchToken() {
        buildStrategy();
        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .bodyToMono(TokenResp.class)
                .block();
    }

    public synchronized void refreshIfNeeded() {
        log.debug("\n\nAttempting to refresh access token\n\n");
        if (Instant.now().isAfter(expiresAt.minusSeconds(30))) {
            log.info("Token expired or near expiry, refreshing...");
            TokenResp newToken = fetchToken();
            if (newToken != null && newToken.accessToken() != null) {
                accessToken = newToken.accessToken();
                long ttl = newToken.expiresIn() != null ? newToken.expiresIn() : 300L;
                expiresAt = Instant.now().plusSeconds(ttl);
                log.info("New token fetched, valid until {}", expiresAt);
            } else {
                throw new IllegalStateException("Failed to refresh token");
            }
        }
    }

    private static boolean blank(String s){ return s == null || s.isBlank(); }

    private static void addIfPresent(BodyInserters.FormInserter<String> data, String k, String v) {
        if (v != null && !v.isBlank()) data.with(k, v);
    }
}
