package com.vodafone.vcs.conductorwrapper.conductor.worker;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.vodafone.vcs.conductorwrapper.action.http.api.HttpActionApi;
import com.vodafone.vcs.conductorwrapper.action.http.dto.XHttpRequest;
import com.vodafone.vcs.conductorwrapper.action.http.enums.XGrantType;
import com.vodafone.vcs.conductorwrapper.action.http.enums.XHttpMethod;
import com.vodafone.vcs.conductorwrapper.action.http.security.OAuth2;
import com.vodafone.vcs.conductorwrapper.common.contract.WrappedWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.Serializable;
import java.util.Map;

@Log4j2
@Component
public class HttpWorker extends WrappedWorker {

    private final HttpActionApi httpActionApi;

    public HttpWorker(HttpActionApi httpActionApi) {
        super("HttpWorker");
        this.httpActionApi = httpActionApi;
    }

    @Override
    public TaskResult doExecute(Task task) {
        TaskResult result = new TaskResult(task);
        Map<String, Object> input = ( Map<String, Object>) task.getInputData().get("data");
        log.info("Input: {}", input);

        String url = input.get("url").toString();
        String method = input.get("method").toString();
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        String connection = input.get("connection").toString();
        log.info("url: {}, method: {}, headers: {}, connection: {}", url, method, headers, connection);

        XHttpRequest request = new XHttpRequest.builder()
                .url(url)
                .withHeader("X-API-VERSION", "1")
                .withHeader("client", "web")
                .connection("keycloak-cli2")
                .headers(headers)
                .method(XHttpMethod.GET)
                .build();

        Map<String, ? extends Serializable> result2 = httpActionApi.execute(request);
        log.info("Result: {}", result2);

        result.getOutputData().put("result", (Map<String, ? extends Serializable>) result2);
        result.setStatus(TaskResult.Status.COMPLETED);
        return result;
    }
}
