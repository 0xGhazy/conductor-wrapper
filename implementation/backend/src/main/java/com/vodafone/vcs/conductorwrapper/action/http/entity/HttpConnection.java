package com.vodafone.vcs.conductorwrapper.action.http.entity;

import com.vodafone.vcs.conductorwrapper.action.http.enums.AuthenticationStrategy;
import com.vodafone.vcs.conductorwrapper.action.http.enums.XGrantType;
import com.vodafone.vcs.conductorwrapper.common.converters.EncryptionConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Table(name = "http_connections", schema = "core")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpConnection {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

    @Enumerated(EnumType.STRING)
    private AuthenticationStrategy strategy;

    @Convert(converter = EncryptionConverter.class) private String apiKey;
    @Convert(converter = EncryptionConverter.class) private String apiKeyHeader;

    @Enumerated(EnumType.STRING)
    private XGrantType grantType;

    @Convert(converter = EncryptionConverter.class) private String clientId;
    @Convert(converter = EncryptionConverter.class) private String username;
    @Convert(converter = EncryptionConverter.class) private String password;
    @Convert(converter = EncryptionConverter.class) private String tokenEndpoint;
    @Convert(converter = EncryptionConverter.class) private String clientSecret;
    @Convert(converter = EncryptionConverter.class) private String scope;
    @Convert(converter = EncryptionConverter.class) private String code;
    @Convert(converter = EncryptionConverter.class) private String codeVerifier;
    private String redirectUri;
    @Builder.Default
    @CreationTimestamp private Instant createdAt = Instant.now();
    @UpdateTimestamp private Instant updatedAt;
}