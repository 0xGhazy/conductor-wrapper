package com.vcs.flowpilot.action.http.api;

import com.vcs.flowpilot.action.http.enums.XHttpMethod;
import com.vcs.flowpilot.action.http.service.HttpConnectionService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Getter
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
    private HttpConnectionService httpConnectionService;

    private XHttpRequest() {}

    // Handle validation and building the HTTP request as planed
    public static class builder {
        private String url;
        private XHttpMethod method;
        private Map<String,String> headers = new HashMap<>();
        private Object body;
        private String connection;

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
