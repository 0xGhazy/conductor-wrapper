package com.vodafone.vcs.conductorwrapper.web.controller;

import com.vodafone.vcs.conductorwrapper.conductor.dto.UIWorkflowExecutionRequest;
import com.vodafone.vcs.conductorwrapper.conductor.service.ConductorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.http.HttpResponse;


@Log4j2
@RestController
@RequestMapping(value = "/api/ui", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UIController {

    private final ConductorService conductorService;

    @PostMapping("/run")
    public ResponseEntity<?> run(@RequestBody UIWorkflowExecutionRequest payload) throws IOException, InterruptedException {
        HttpResponse<String> result = conductorService.parseWorkflow(payload);
        return ResponseEntity.ok(result);
    }
}
