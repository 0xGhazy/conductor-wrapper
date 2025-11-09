package com.vodafone.vcs.conductorwrapper.conductor.worker;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.vodafone.vcs.conductorwrapper.action.http.api.HttpActionApi;
import com.vodafone.vcs.conductorwrapper.action.http.dto.XHttpRequest;
import com.vodafone.vcs.conductorwrapper.action.http.enums.XHttpMethod;
import com.vodafone.vcs.conductorwrapper.common.contract.WrappedWorker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import static com.vodafone.vcs.conductorwrapper.common.WorkerInputValidator.*;

@Log4j2
@Component
public class HttpWorker extends WrappedWorker {

    private final HttpActionApi httpActionApi;
    private static final String K_HTTP_CONFIGS = "HttpConfigs";
    private static final String K_URL = "url";
    private static final String K_METHOD = "method";
    private static final String K_HEADERS = "headers";
    private static final String K_BODY = "body";
    private static final String K_QUERY = "query";
    private static final String K_CONNECTION = "connection";
    private static final String DEFAULT_CONN = "--NONE--";

    public HttpWorker(HttpActionApi httpActionApi) {
        super("HttpWorker");
        this.httpActionApi = httpActionApi;
    }

    @Override
    public TaskResult doExecute(Task task) {
        TaskResult result = new TaskResult(task);
        HTTPActionConfig cfg = inputValidation(task);

        if (!cfg.isValidConfig()) {
            log.warn("Http worker input validation failed due to: {}", cfg.getReason());
            result.setStatus(TaskResult.Status.FAILED);
            result.setReasonForIncompletion(cfg.getReason());
            return result;
        }

        Map<String, String> safeHeaders = redactHeaders(cfg.getHeaders());
        int bodySize = safeBodySize(cfg.getBody());

        log.info("HTTP worker request start method={} url={} headers={} body_size={}B connection={}",
                cfg.getMethod(), cfg.getUrl(), safeHeaders, bodySize, cfg.getConnection());

        XHttpRequest request = new XHttpRequest.builder()
                .url(cfg.getUrl())
                .headers(cfg.getHeaders())
                .connection(cfg.getConnection())
                .method(XHttpMethod.valueOf(cfg.getMethod()))
                .body(cfg.getBody())
                .build();

        Map<String, Object> response = httpActionApi.execute(request);
        log.debug("Response success: {}, Response status: {}, ", response.get("success"), response.get("status"));

        result.getOutputData().put("result", response);
        result.setStatus(TaskResult.Status.COMPLETED);
        log.info("Worker job completed successfully - Task status: {}", result.getStatus());
        return result;
    }

    private HTTPActionConfig inputValidation(Task task) {
        log.debug("Attempting to validate HTTP worker task: {}", task == null ? null : task.getInputData());

        if (task == null || task.getInputData() == null) return invalid("data is missing, expected object but got null");

        Map<String, Object> cfg = toStringObjectMap(task.getInputData().get(K_HTTP_CONFIGS));

        String url = getString(cfg, K_URL);
        if (url.isBlank()) return invalid("Url is required field");

        String method = getString(cfg, K_METHOD).toUpperCase();
        if (method.isBlank() || !inSupportedMethods(method)) return invalid("Unsupported HTTP method");

        Map<String, String> headers = toStringStringMap(cfg.get(K_HEADERS));
        Map<String, Object> body   = toStringObjectMap(cfg.get(K_BODY));
        Map<String, String> query  = toStringStringMap(cfg.get(K_QUERY));
        String connection          = defaultIfBlank(getString(cfg, K_CONNECTION), DEFAULT_CONN);

        String finalUrl = buildUrlWithParams(url, query);

        return HTTPActionConfig.builder()
                .isValidConfig(true)
                .method(method)
                .headers(headers)
                .body(body)
                .query(query)
                .connection(connection)
                .url(finalUrl)
                .build();
    }

    private static Map<String,String> redactHeaders(Map<String,String> in) {
        if (in == null || in.isEmpty()) return Map.of();
        var copy = new java.util.LinkedHashMap<String,String>(in.size());
        in.forEach((k,v) -> {
            String key = k == null ? "" : k;
            if (key.equalsIgnoreCase("authorization") || key.equalsIgnoreCase("cookie")) {
                copy.put(key, "***REDACTED***");
            } else {
                copy.put(key, v);
            }
        });
        return copy;
    }

    private static int safeBodySize(Map<String, Object> body) {
        if (body == null) return 0;
        try {
            // rough size without leaking body content in INFO logs
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(body).length;
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean inSupportedMethods(String method) {
        return List.of("POST", "GET", "PATCH", "PUT", "DELETE", "OPTIONS").contains(method);
    }

    private HTTPActionConfig invalid(String reason) {
        return HTTPActionConfig.builder().isValidConfig(false).reason(reason).build();
    }

    private static String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) return baseUrl;

        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(baseUrl.contains("?") ? "&" : "?");

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) sb.append("&");
            first = false;
            String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            sb.append(key).append("=").append(value);
        }

        return sb.toString();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class HTTPActionConfig {
        private boolean isValidConfig;
        private String reason;
        private String url;
        private String method;
        private Map<String, String> headers;
        private Map<String, Object> body;
        private Map<String, String> query;
        private String connection;

        @Override
        public String toString() {
            return "{" +
                    "isValidConfig=" + isValidConfig +
                    ", reason='" + reason + '\'' +
                    ", url='" + url + '\'' +
                    ", method='" + method + '\'' +
                    ", headers=" + headers +
                    ", body=" + body +
                    ", query=" + query +
                    ", connection='" + connection + '\'' +
                    '}';
        }
    }

}
