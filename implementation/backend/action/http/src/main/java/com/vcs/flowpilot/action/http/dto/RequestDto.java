package com.vcs.flowpilot.action.http.dto;

import com.vcs.flowpilot.action.http.enums.XHttpMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    @NotBlank(message = "Request Url is required")
    private String url;
    @NotNull(message = "Request method is required")
    private XHttpMethod method;
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    @Builder.Default
    private Object body = new HashMap<>();
    @Positive(message = "Request timeout must be positive number of seconds")
    private Integer timeout;
    private String connection;
}
