package com.vodafone.vcs.conductorwrapper.action.http.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class XHttpRequest {

    @NotBlank(message = "Node name is required")
    private String nodeName;

    @NotBlank(message = "Url is required")
    private String url;

    @NotNull(message = "Method is required")
    @Enumerated(EnumType.STRING)
    private HttpMethod method;

    @Singular("withHeader")
    @NotNull(message = "Headers is not nullable")
    private Map<String, String> headers;

    @Singular("withQuery")
    @NotNull(message = "Queries is not nullable")
    private Map<String, String> queries;

    @NotNull(message = "body is not nullable")
    private Object body;

    @Builder.Default
    @Positive(message = "Timeout must be positive number of ms")
    private Integer timeoutInMilliSeconds = 5000;

    @NotNull(message = "Connection is not nullable")
    private String connection;
}
