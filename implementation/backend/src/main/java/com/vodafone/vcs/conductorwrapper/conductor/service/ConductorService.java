package com.vodafone.vcs.conductorwrapper.conductor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodafone.vcs.conductorwrapper.conductor.dto.SimpleTaskDto;
import com.vodafone.vcs.conductorwrapper.conductor.dto.UIWorkflowExecutionRequest;
import com.vodafone.vcs.conductorwrapper.conductor.dto.WorkflowAction;
import com.vodafone.vcs.conductorwrapper.conductor.enums.ActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class ConductorService {

    private final ObjectMapper mapper;

    // TODO: read this from database or API call
    // TODO: Recreate tasks with descriptive names
    private final Map<ActionType, String> actionHandlers = Map.of(
            ActionType.HTTP_ACTION, "HTTP_Workflow_Only",
            ActionType.DATABASE_ACTION, "DATABASE_WORKFLOW"
    );

    public HttpResponse<String> parseWorkflow(UIWorkflowExecutionRequest payload) throws IOException, InterruptedException {
        Map<String, SimpleTaskDto> tasks = new HashMap<>();
        Map<String, Object> actionConfigs = new HashMap<>();

        // remove start and terminate
        List<UIWorkflowExecutionRequest.Node> nodes = payload.getNodes();
        nodes.remove(0);
        nodes.remove(nodes.size() - 1);
        log.info("Attempting to parse {} nodes", nodes.size());

        HashMap<String, Object> body = new HashMap<>();
        for (UIWorkflowExecutionRequest.Node node: nodes) {

            // Skip start and the terminate flags
            if (node.getId().equals("start") || node.getId().startsWith("terminate")) continue;

            String nodeType = node.getType();
            Map<String, Object> config = node.getData().getConfig();
            HashMap<String, Object> requestData = new HashMap<>();
            SimpleTaskDto task = new SimpleTaskDto();

            switch (nodeType) {
                case "httpAction" -> {
                    requestData.put("url", config.get("url"));
                    requestData.put("method", config.get("method"));
                    requestData.put("headers", config.get("headers"));
                    requestData.put("body", config.get("body"));
                    requestData.put("query", config.get("query"));
                    requestData.put("connection", config.get("connection"));
                    task.setName("HttpWorker");
                    task.setRefName(node.getId());
                    task.setInputParameters(requestData);
                    actionConfigs.put("HttpConfigs", requestData);
                }
                case "dbAction" -> {
                    requestData.put("queryId", config.get("query"));
                    requestData.put("params", config.getOrDefault("params", Map.of()));
                    task.setName("DatabaseWorker");
                    task.setRefName(node.getId());
                    task.setInputParameters(requestData);
                    actionConfigs.put("DatabaseConfigs", requestData);
                }
                default -> {

                }
            }
            tasks.put(task.getRefName(), task);
        }
        log.info(actionConfigs);

        body.put("input", Map.of("data", actionConfigs));
        body.put("name", "HTTP_DB_Workflow");
        body.put("priority", 0);
        body.put("rateLimited", false);
        body.put("workflowVersion", 2);

        byte[] json = mapper.writeValueAsBytes(body);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/workflow"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(json))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return res;
    }


    public void executeWorkflow(WorkflowAction workflowAction) throws IOException, InterruptedException {
        String taskName = actionHandlers.get(workflowAction.getType());
        ActionType at = workflowAction.getType();
        HashMap<String, Object> body = new HashMap<>();

        HashMap<String, Object> requestData = new HashMap<>();
        if (ActionType.HTTP_ACTION.equals(at)) {
            HashMap<String, Object> config = workflowAction.getConfig();
            requestData.put("url", config.get("url"));
            requestData.put("method", config.get("method"));
            requestData.put("headers", config.get("headers"));
            requestData.put("body", config.get("body"));
            requestData.put("query", config.get("query"));
            requestData.put("connection", config.get("connection"));
            body.put("input", Map.of("data", requestData));
        } else if (ActionType.DATABASE_ACTION.equals(at)) {
            HashMap<String, Object> config = workflowAction.getConfig();
            requestData.put("queryId", config.get("queryId"));
            requestData.put("params", config.getOrDefault("params", Map.of()));
            body.put("input", Map.of("data", Map.of("DatabaseConfigs", requestData)));
        }

        body.put("name", taskName);
        body.put("priority", 0);
        body.put("rateLimited", false);
        body.put("workflowVersion", 2);

        byte[] json = mapper.writeValueAsBytes(body);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/workflow"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(json))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
    }



}
