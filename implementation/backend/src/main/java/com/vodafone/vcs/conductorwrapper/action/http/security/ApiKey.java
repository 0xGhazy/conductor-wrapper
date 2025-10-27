package com.vodafone.vcs.conductorwrapper.action.http.security;

import com.vodafone.vcs.conductorwrapper.action.http.entity.HttpConnection;
import com.vodafone.vcs.conductorwrapper.common.contract.AuthStrategy;
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

    public ApiKey(HttpConnection conn) {
        this.apiKey = conn.getApiKey();
        this.header = conn.getApiKeyHeader();
    }

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
