package com.conductor.core.conductor.metadata.controller;


import com.conductor.core.conductor.metadata.dto.WorkflowDefDto;
import com.conductor.core.conductor.metadata.service.MetaDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Log4j2
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final MetaDataService metaDataService;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDefDto> createOrUpdateTask(@Valid @RequestBody WorkflowDefDto payload) {
        long startTime = System.currentTimeMillis();
        log.info("Create new workflow definition request received");
        log.debug("Request payload: {}", payload);
        WorkflowDefDto result = metaDataService.createWorkflowDefinition(payload);
        URI location = URI.create("/api/workflows/" + result.getName());
        long duration = System.currentTimeMillis() - startTime;
        log.info("Create new workflow definition completed successfully - duration {}ms", duration);
        return ResponseEntity.created(location).body(result);
    }

//    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<TaskDto> getTaskByName(@PathVariable("name") String name) {
//        long startTime = System.currentTimeMillis();
//        log.info("Fetching task definition by name request received - name={}", name);
//        TaskDto result = metaDataService.fetchTaskDefinitionByName(name);
//        long duration = System.currentTimeMillis() - startTime;
//        log.info("Fetching task definition by name completed successfully - duration {}ms", duration);
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<TaskDto>> getAllTasks() {
//        long startTime = System.currentTimeMillis();
//        log.info("Fetching all task definition request received");
//        List<TaskDto> result = metaDataService.fetchAllTaskDefinitions();
//        long duration = System.currentTimeMillis() - startTime;
//        log.info("Fetching all task definition completed successfully - duration {}ms", duration);
//        return ResponseEntity.ok(result);
//    }
//
//    @DeleteMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> deleteTaskDefinitionByName(@PathVariable("name") String name) {
//        long startTime = System.currentTimeMillis();
//        log.info("Deleting task definition by name request received - name={}", name);
//        metaDataService.deleteTaskDefinition(name);
//        long duration = System.currentTimeMillis() - startTime;
//        log.info("Deleting task definition by name completed successfully - duration {}ms", duration);
//        return ResponseEntity.noContent().build();
//    }
}
