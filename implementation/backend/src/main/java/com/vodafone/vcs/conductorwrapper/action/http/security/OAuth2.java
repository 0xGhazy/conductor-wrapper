package com.vodafone.vcs.conductorwrapper.action.http.security;

import com.vodafone.vcs.conductorwrapper.action.http.dto.TokenResponse;
import com.vodafone.vcs.conductorwrapper.action.http.entity.HttpConnection;
import com.vodafone.vcs.conductorwrapper.common.contract.AuthStrategy;
import com.vodafone.vcs.conductorwrapper.action.http.enums.XGrantType;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static java.nio.charset.StandardCharsets.UTF_8;

@Log4j2
@Builder
@AllArgsConstructor
public class OAuth2 implements AuthStrategy {
    private WebClient webClient = WebClient.builder().build();
    @Getter
    private volatile String accessToken;
    private volatile String name;
    @Getter
    private volatile String refreshToken;
    @Getter
    @Builder.Default
    private volatile Instant expiresAt = Instant.EPOCH;
    private XGrantType grantType;
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String username;
    private String password;
    private String code;
    private String redirectUri;
    private String codeVerifier;
    private BodyInserters.FormInserter<String> form;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "oauth2-refresh");
                t.setDaemon(true);
                return t;
            });
    private final AtomicReference<ScheduledFuture<?>> refreshTask = new AtomicReference<>();

    public OAuth2(HttpConnection src) {
        this.accessToken = null;
        this.refreshToken = null;
        this.expiresAt   = java.time.Instant.EPOCH;
        this.grantType     = src.getGrantType();
        this.tokenEndpoint = src.getTokenEndpoint();
        this.clientId      = src.getClientId();
        this.clientSecret  = src.getClientSecret();
        this.scope         = src.getScope();
        this.username      = src.getUsername();
        this.password      = src.getPassword();
        this.code          = src.getCode();
        this.redirectUri   = src.getRedirectUri();
        this.codeVerifier  = src.getCodeVerifier();
        this.name          = src.getName();
        log.info("register {}", name);
        this.webClient = WebClient.builder().build();
        testAuthentication();
    }


    @Override
    public WebClient.RequestHeadersSpec<?> apply(WebClient.RequestHeadersSpec<?> req) {
        validateRequired();
        accessToken = getAccessToken();
        return req.headers(h -> h.setBearerAuth(accessToken));
    }

    @Override
    public boolean testAuthentication() {
        return fetchToken(false) != null;
    }

    public TokenResponse fetchToken(boolean isRefreshToken) {
//        log.debug("Attempting to fetch {} token", isRefreshToken ? "refresh" : "access");
        TokenResponse response = null;
        try {
            if (isRefreshToken) {
                response = fetchRefreshToken();
            } else {
                response = fetchAccessToken();
            }
            seed(response);
//            log.debug("{} fetch successfully", isRefreshToken ? "refresh" : "access");
        } catch (WebClientResponseException.MethodNotAllowed e) {
            log.error("Web client MethodNotAllowed exception occurred: {}", e.getMessage());

        } catch (WebClientResponseException e) {
            log.error("Generic Web client exception occurred: {}", e.getMessage());
        }
        return response;
    }

    private void seed(TokenResponse t) {
        this.accessToken  = t.accessToken();
        this.refreshToken = t.refreshToken();
        long expiresIn = Math.max(1, t.expiresIn());
        this.expiresAt = Instant.now().plusSeconds(expiresIn);
        scheduleAutoRefresh(expiresIn);
    }

    private void scheduleAutoRefresh(long expiresInSec) {
        long delaySec = Math.max(0, expiresInSec - 5);
        ScheduledFuture<?> next = scheduler.schedule(this::autoRefreshTick, delaySec, TimeUnit.SECONDS);
        ScheduledFuture<?> prev = refreshTask.getAndSet(next);
        if (prev != null) prev.cancel(false);
    }

    private void autoRefreshTick() {
        try {
            TokenResponse t = (refreshToken != null) ? fetchToken(true) : fetchToken(false);
            this.accessToken  = t.accessToken();
//            log.info("Access Token for {}: {}", name, accessToken);
            if (t.refreshToken() != null && !t.refreshToken().isBlank()) this.refreshToken = t.refreshToken();
            long expiresIn = Math.max(1, t.expiresIn());
            this.expiresAt = Instant.now().plusSeconds(expiresIn);
            scheduleAutoRefresh(expiresIn);
        } catch (Exception e) {
            log.error("Auto-refresh failed: {}", e.toString());
            ScheduledFuture<?> next = scheduler.schedule(this::autoRefreshTick, 10, TimeUnit.SECONDS);
            ScheduledFuture<?> prev = refreshTask.getAndSet(next);
            if (prev != null) prev.cancel(false);
        }
    }

    @Override
    public String toString() {
        return "AuthStrategy.OAuth2{" +
                "webClient=" + webClient +
                ", accessToken='" + accessToken + '\'' +
                ", expiresAt=" + expiresAt +
                ", grantType=" + grantType +
                ", tokenEndpoint='" + tokenEndpoint + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", scope='" + scope + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", code='" + code + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", codeVerifier='" + codeVerifier + '\'' +
                ", form=" + form +
                '}';
    }

    private TokenResponse fetchAccessToken() {
        buildStrategy();
        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();
    }

    private TokenResponse fetchRefreshToken() {
        form = BodyInserters
                .fromFormData("client_id", clientId)
                .with("refresh_token", URLEncoder.encode(refreshToken, UTF_8))
                .with("grant_type", "refresh_token");
        addIfPresent(form, "client_secret", clientSecret);
        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();
    }

    public synchronized void refreshIfNeeded() {
        log.debug("Attempting to refresh access token");
        if (Instant.now().isAfter(expiresAt.minusSeconds(30))) {
            log.info("Token expired or near expiry, refreshing...");
            TokenResponse newToken = fetchToken(true);
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

}
