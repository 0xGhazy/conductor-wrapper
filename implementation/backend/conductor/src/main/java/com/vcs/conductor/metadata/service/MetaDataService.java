package com.vcs.conductor.metadata.service;

import com.netflix.conductor.client.exception.ConductorClientException;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.vcs.conductor.exception.DuplicateWorkflowNameException;
import com.vcs.conductor.exception.ServerConnectionException;
import com.vcs.conductor.exception.TaskNotFoundException;
import com.vcs.conductor.metadata.adapter.TaskAdapter;
import com.vcs.conductor.metadata.adapter.WorkflowAdapter;
import com.vcs.conductor.metadata.dto.TaskDefDto;
import com.vcs.conductor.metadata.dto.TaskDto;
import com.vcs.conductor.metadata.dto.WorkflowDefDto;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class MetaDataService {

    private final MetadataClient metadataClient = new MetadataClient();
    private final RestTemplate restTemplate;
    private final TaskAdapter taskAdapter;
    private final WorkflowAdapter workflowAdapter;
    @Value("${conductor.api.base.url}")
    String baseUrl;

    @PostConstruct
    private void init() {
        metadataClient.setRootURI(baseUrl);
        log.info("Metadata cclient is initialized successfully");
    }


    @Transactional
    public TaskDefDto createOrUpdateTaskDefinition(@Valid TaskDefDto dto) {
        log.debug("Attempting to store/update task definition into conductor database");

        log.debug("Converting task DTO to task definition");
        TaskDef task = taskAdapter.toTaskDef(dto);
        log.debug("Task converted successfully");

        try {
            task = metadataClient.getTaskDef(dto.getName());
            log.debug("Flagged as update request, Set update timestamp");
            task.setUpdatedBy("Conductor Wrapper");
            task.setUpdateTime(System.currentTimeMillis());
        } catch (ConductorClientException ex) {
            log.warn("Conductor exception: {}", ex.getMessage());
            if (ex.getMessage().contains("No such taskType found by name")) {
                task.setCreateTime(System.currentTimeMillis());
                task.setCreatedBy("Conductor Wrapper");
            }
        }

        metadataClient.registerTaskDefs(List.of(task));
        log.debug("Task definition created and registered successfully");

        return taskAdapter.toTaskDto(task);
    }


    public TaskDefDto fetchTaskDefinitionByName(@NotBlank(message = "Name is required") String name) {
        log.debug("Attempting to fetch task with name={}", name);

        TaskDef task = new TaskDef();
        try {
            task =  metadataClient.getTaskDef(name);
        } catch (ConductorClientException ex) {
            if (ex.getMessage().contains("No such taskType found by name")) {
                throw new TaskNotFoundException("Task not found");
            }
        }
        return taskAdapter.toTaskDto(task);
    }


    public List<TaskDefDto> fetchAllTaskDefinitions() {
        log.debug("Attempting to fetch all task definitions");

        String apiUrl = baseUrl + "metadata/taskdefs";
        log.debug("Attempting to call metadata client via HTTP with url={}", apiUrl);

        List<TaskDefDto> body;
        HttpStatus sc = HttpStatus.OK;
        try {
            ResponseEntity<List<TaskDefDto>> resp = restTemplate.exchange(apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null) {
                body = resp.getBody();
                log.debug("API response size = {}, API response status code = {}", body.size(), sc);
            }
            return resp.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Failed to call metadata clint with status code = {}, Maybe conductor is down right now.", HttpStatus.NOT_FOUND);
            log.error("Conductor response - {}", ex.getMessage());
            throw new ServerConnectionException("Failed to call metadata client, Maybe conductor is down rightnow");
        }
    }

    public void deleteTaskDefinition(@NotBlank(message = "Name is required") String name) {
        log.debug("Attempting to delete task definition by name={}", name);

        log.debug("Fetching task definition by name to validate task existence");
        fetchTaskDefinitionByName(name);
        log.debug("Task definition is exist and ready to be deleted");

        log.debug("Proceed to delete task definition");
        String apiUrl = baseUrl + "metadata/taskdefs/" + name;
        log.debug("Attempting to call metadata delete via HTTP with url={}", apiUrl);
        restTemplate.delete(apiUrl);
        log.debug("Task definition deleted successfully");
    }


    /************************************** Workflow Methods **************************************/


    public Optional<WorkflowDefDto> fetchWorkflowDefinitionByName(@NotBlank(message = "Name is required")String name) {
        log.debug("Attempting to fetch workflow definition by name={}", name);
        log.debug("Fetching workflow definition by name to validate workflow existence");
        String apiUrl = baseUrl + "metadata/workflow/" + name;
        WorkflowDef body;
        try {
            ResponseEntity<WorkflowDef> resp = restTemplate.exchange(apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null) {
                body = resp.getBody();
                log.debug("Fetch workflow API response = {}, API response status code = {}", body, resp.getStatusCode());
            }
            return Optional.of(workflowAdapter.toDto(resp.getBody()));
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Failed to call metadata workflow API with status code = {}, Maybe conductor is down right now.", HttpStatus.NOT_FOUND);
            log.error("Metadata workflow API response - {}", ex.getMessage());
        }
        return Optional.empty();
    }


    public WorkflowDefDto createWorkflowDefinition(@Valid WorkflowDefDto payload) {
        log.debug("Attempting to store/update workflow definition into conductor database");

        log.debug("Validating workflow existence by name");
        Optional<WorkflowDefDto> workflowDefOptional = fetchWorkflowDefinitionByName(payload.getName());
        if (workflowDefOptional.isPresent()) {
            log.debug("Request failed due to the workflow name is already exist");
            throw new DuplicateWorkflowNameException("Workflow definition is already exist");
        }

        WorkflowDef workflowDef = workflowAdapter.toDefinition(payload);
        workflowDef.setCreateTime(System.currentTimeMillis());
        workflowDef.setCreatedBy("Conductor Wrapper");

        List<TaskDto> taskDtos = payload.getTasks();
        List<WorkflowTask> tasks = new ArrayList<>();

        for (TaskDto dto: taskDtos){
            WorkflowTask wt = taskAdapter.toTask(dto);
            tasks.add(wt);
        }

        workflowDef.setTasks(tasks);
        metadataClient.registerWorkflowDef(workflowDef);
        log.info("Workflow definition created successfully");

        return workflowAdapter.toDto(workflowDef);
    }
}
