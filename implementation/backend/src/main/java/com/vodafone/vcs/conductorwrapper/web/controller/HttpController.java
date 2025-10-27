package com.vodafone.vcs.conductorwrapper.web.controller;

import com.vodafone.vcs.conductorwrapper.action.http.dto.XHttpRequest;
import com.vodafone.vcs.conductorwrapper.action.http.dto.RequestDto;
import com.vodafone.vcs.conductorwrapper.action.http.api.HttpActionApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/action/http")
@RequiredArgsConstructor
public class HttpController {

    private final HttpActionApi service;

    @PostMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    public String executeRequest(@Valid @RequestBody RequestDto payload) {
        log.info("Payload: {}", payload);

        return "";
    }
}
