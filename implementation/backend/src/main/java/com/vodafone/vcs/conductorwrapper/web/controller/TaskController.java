package com.vodafone.vcs.conductorwrapper.web.controller;

import com.vodafone.vcs.conductorwrapper.conductor.metadata.dto.TaskDefDto;
import com.vodafone.vcs.conductorwrapper.conductor.metadata.service.MetaDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final MetaDataService metaDataService;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDefDto> createOrUpdateTask(@Valid @RequestBody TaskDefDto payload) {
        long startTime = System.currentTimeMillis();
        log.info("Create new task definition request received");
        log.debug("Request payload: {}", payload);
        TaskDefDto result = metaDataService.createOrUpdateTaskDefinition(payload);
        URI location = URI.create("/api/tasks/" + result.getName());
        long duration = System.currentTimeMillis() - startTime;
        log.info("Create new task definition completed successfully - duration {}ms", duration);
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDefDto> getTaskByName(@PathVariable("name") String name) {
        long startTime = System.currentTimeMillis();
        log.info("Fetching task definition by name request received - name={}", name);
        TaskDefDto result = metaDataService.fetchTaskDefinitionByName(name);
        long duration = System.currentTimeMillis() - startTime;
        log.info("Fetching task definition by name completed successfully - duration {}ms", duration);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDefDto>> getAllTasks() {
        long startTime = System.currentTimeMillis();
        log.info("Fetching all task definition request received");
        List<TaskDefDto> result = metaDataService.fetchAllTaskDefinitions();
        long duration = System.currentTimeMillis() - startTime;
        log.info("Fetching all task definition completed successfully - duration {}ms", duration);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteTaskDefinitionByName(@PathVariable("name") String name) {
        long startTime = System.currentTimeMillis();
        log.info("Deleting task definition by name request received - name={}", name);
        metaDataService.deleteTaskDefinition(name);
        long duration = System.currentTimeMillis() - startTime;
        log.info("Deleting task definition by name completed successfully - duration {}ms", duration);
        return ResponseEntity.noContent().build();
    }

}
