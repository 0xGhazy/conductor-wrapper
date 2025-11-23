package com.vodafone.vcs.conductorwrapper.action.http.dto;

import com.vodafone.vcs.conductorwrapper.action.http.enums.AuthenticationStrategy;
import com.vodafone.vcs.conductorwrapper.action.http.enums.XGrantType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HTTPConnectionDTO {
    private UUID id;
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Connection name must be between (3~50) char")
    private String name;
    @NotNull(message = "Strategy is required (OAUTH2|API_KEY)")
    @Enumerated(EnumType.STRING)
    private AuthenticationStrategy strategy;
    private String apiKey;
    private String apiKeyHeader;
    @Enumerated(EnumType.STRING)
    private XGrantType grantType;
    private String clientId;
    private String username;
    private String password;
    private String tokenEndpoint;
    private String clientSecret;
    private String scope;
    private String code;
    private String codeVerifier;
    private String redirectUri;
    @Builder.Default private Instant createdAt = Instant.now();
    private Instant updatedAt;

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", strategy=" + strategy +
                ", apiKey='" + apiKey + '\'' +
                ", apiKeyHeader='" + apiKeyHeader + '\'' +
                ", grantType=" + grantType +
                ", clientId='" + clientId + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", tokenEndpoint='" + tokenEndpoint + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", scope='" + scope + '\'' +
                ", code='" + code + '\'' +
                ", codeVerifier='" + codeVerifier + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
