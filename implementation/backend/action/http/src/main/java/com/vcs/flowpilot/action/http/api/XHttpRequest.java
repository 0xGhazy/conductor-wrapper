package com.vcs.flowpilot.action.http.api;

import com.vcs.flowpilot.action.http.security.contract.AuthStrategy;
import com.vcs.flowpilot.action.http.enums.XHttpMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Builder
@Service
@AllArgsConstructor
@EqualsAndHashCode
public class XHttpRequest {
    private String flowId;
    private String url;
    private HttpMethod method;
    private Map<String, String> headers;
    private Object body;
    private String connection;
    private boolean isBuilt = false;
    private AuthStrategyManager authStrategyManager;

    private XHttpRequest() {}

    // Handle validation and building the HTTP request as planed
    public static class builder {
        private String url;
        private XHttpMethod method;
        private Map<String,String> headers = new HashMap<>();
        private Object body;
        private String connection;
        private AuthStrategyManager authStrategyManager;

        public builder url(@NotBlank(message = "Request url is required") String url) {
            this.url = url;
            return this;
        }

        public builder method(@NotBlank(message = "Request method is required") XHttpMethod method) {
            this.method = method;
            return this;
        }

        public builder withHeader(@NotBlank(message = "Request header name is required") String header,
                                  @NotBlank(message = "Request header value is required") String value) {
            this.headers.put(header, value);
            return this;
        }

        public builder headers(@NotNull(message = "Request headers can not be null") Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public builder body(Object body) {
            this.body = body;
            return this;
        }

        public builder authStrategyManager(AuthStrategyManager authStrategyManager) {
            this.authStrategyManager = authStrategyManager;
            return this;
        }

        public builder connection(String connection) {
            this.connection = connection;
            return this;
        }

        public XHttpRequest build() {
            // --- validation
            if (!StringUtils.hasText(url)) {
                throw new IllegalArgumentException("Request url is required");
            }
            if (method == null) {
                throw new IllegalArgumentException("Request method is required");
            }
            // --- construct
            XHttpRequest x = new XHttpRequest();
            x.url = this.url;
            x.method = map(method);
            x.headers = (this.headers == null) ? new HashMap<>() : new HashMap<>(this.headers);
            x.body = this.body;
            x.connection = this.connection;
            x.authStrategyManager = this.authStrategyManager;
            x.isBuilt = true;
            return x;
        }

        private HttpMethod map(XHttpMethod m) {
            return switch (Objects.requireNonNull(m)) {
                case GET -> HttpMethod.GET;
                case POST -> HttpMethod.POST;
                case PUT -> HttpMethod.PUT;
                case PATCH -> HttpMethod.PATCH;
                case DELETE -> HttpMethod.DELETE;
                case OPTIONS -> HttpMethod.OPTIONS;
                case HEAD -> HttpMethod.HEAD;
                case TRACE -> HttpMethod.TRACE;
            };
        }
    }

    public String execute(WebClient client) {
        ensureBuilt();
        WebClient wc = (client != null) ? client : WebClient.builder().build();

        WebClient.RequestBodySpec spec = wc
                .method(this.method)
                .uri(this.url)
                .accept(MediaType.APPLICATION_JSON);

        if (headers != null && !headers.isEmpty()) {
            spec = spec.headers(h -> h.setAll(headers));
        }

        WebClient.RequestHeadersSpec<?> req =
                (body == null ? spec : spec.bodyValue(body));

        if (connection != null) {
            req = authStrategyManager.applyAuthStrategy(connection ,req);
        }

        return req.retrieve().bodyToMono(String.class).block();
    }


    private void ensureBuilt() {
        if (!isBuilt) {
            throw new IllegalStateException("XHttpRequest must be built via builder.build() before execute()");
        }
    }


    @Override
    public String toString() {
        return "XHttpRequest{" +
                "url=" + url +
                ", method=" + method +
                ", headers=" + headers +
                ", body=" + body +
                ", connection=" + connection +
                ", hash=" + hashCode() +
                '}';
    }
}
