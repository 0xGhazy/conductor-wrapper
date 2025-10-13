package com.vcs.flowpilot.action.http.security;

import com.vcs.flowpilot.action.http.security.contract.AuthStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@Builder
@AllArgsConstructor
public class ApiKey implements AuthStrategy {
    private String header;
    private String apiKey;
    private long expiry = Long.MAX_VALUE;

    @Override
    public WebClient.RequestHeadersSpec<?> apply(WebClient.RequestHeadersSpec<?> req) {
        if (header == null || header.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("header and apiKey are required");
        }
        return req.header(header, apiKey);
    }

    @Override
    public boolean testAuthentication() {
        return true;
    }
}
