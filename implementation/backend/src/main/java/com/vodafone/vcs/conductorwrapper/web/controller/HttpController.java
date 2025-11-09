package com.vodafone.vcs.conductorwrapper.web.controller;

import com.vodafone.vcs.conductorwrapper.action.http.api.HttpActionApi;
import com.vodafone.vcs.conductorwrapper.conductor.dto.WorkflowAction;
import com.vodafone.vcs.conductorwrapper.conductor.service.ConductorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping(value = "/api/actions/http", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class HttpController {

    private final HttpActionApi service;
    private final ConductorService conductorService;

    @PostMapping("/execute")
    public ResponseEntity<?> execute(@Valid @RequestBody WorkflowAction payload) throws IOException, InterruptedException {
        log.info("HTTP request execution received - payload={}", payload);
        conductorService.executeWorkflow(payload);
        return ResponseEntity.ok(Map.of("ok", true));
    }


    @GetMapping("/connections")
    public ResponseEntity<?> listConnections() {
        return ResponseEntity.ok(service.listConnections());
    }
}
