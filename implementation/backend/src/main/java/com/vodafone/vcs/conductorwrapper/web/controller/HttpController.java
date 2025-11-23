package com.vodafone.vcs.conductorwrapper.web.controller;

import com.vodafone.vcs.conductorwrapper.action.http.dto.HTTPConnectionDTO;
import com.vodafone.vcs.conductorwrapper.action.http.dto.ShortDetailedHttpConnectionDTO;
import com.vodafone.vcs.conductorwrapper.action.http.dto.XHttpRequest;
import com.vodafone.vcs.conductorwrapper.action.http.enums.AuthenticationStrategy;
import com.vodafone.vcs.conductorwrapper.action.http.exception.InvalidConnectionException;
import com.vodafone.vcs.conductorwrapper.action.http.service.HttpService;
import com.vodafone.vcs.conductorwrapper.common.response.Response;
import com.vodafone.vcs.conductorwrapper.conductor.service.ConductorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Log4j2
@RestController
@RequestMapping(value = "/api/actions/http",
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class HttpController {

    private final HttpService service;
    private final ConductorService conductorService;

    @PostMapping("/execute")
    public ResponseEntity<Response> execute(@Valid @RequestBody XHttpRequest request) {
        log.info("Executing HTTP request for {} node", request.getNodeName());
        Map<String, Object> result = service.execute(request);
        int statusCode = (int) result.get("status");
        HttpStatus status = statusCode > 1? HttpStatus.valueOf(statusCode): HttpStatus.BAD_REQUEST;
        Response response = Response.builder()
                .data(result.get("body"))
                .message("Request executed successfully")
                .requestId(MDC.get("RID"))
                .status(status)
                .build();
        log.info("Node {} HTTP request execution finished successfully", request.getNodeName());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping("/connections")
    public ResponseEntity<?> registerAuthenticationStrategy(@Valid @RequestBody HTTPConnectionDTO payload) {
        log.info("HTTP action connection register request received - payload={}", payload);
        validateAuthStrategyDto(payload);
        ShortDetailedHttpConnectionDTO result = service.registerConnection(payload);
        Response response = Response.builder()
                .data(result)
                .message("Connection registered successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.CREATED)
                .build();
        log.info("HTTP action connection register completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

//    @GetMapping("/connections")
//    public ResponseEntity<Response> listConnections() {
//        log.info("HTTP action connections fetching request received");
//        Set<String> result = service.listConnections();
//        Response response = Response.builder()
//                .data(result)
//                .message("Connections fetched successfully")
//                .requestId(MDC.get("RID"))
//                .status(HttpStatus.OK)
//                .build();
//        log.info("HTTP action connections fetching completed successfully");
//        return new ResponseEntity<>(response, response.getStatus());
//    }

    @GetMapping("/connections")
    public ResponseEntity<Response> fetchConnectionsWithState() {
        log.info("HTTP action connections states fetching request received");
        List<ShortDetailedHttpConnectionDTO> result = service.listConnectionsWithStates();
        Response response = Response.builder()
                .data(result)
                .message("Connections states fetched successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .build();
        log.info("HTTP action connections states fetching completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping("/connections/{name}")
    public ResponseEntity<Response> fetchConnectionByName(@PathVariable("name") String name) {
        name = name.toUpperCase();
        log.info("HTTP action connection fetching by name request received - name={}", name);
        HTTPConnectionDTO result = service.fetchConnectionByName(name);
        Response response = Response.builder()
                .data(result)
                .message("Connection fetched successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .build();
        log.info("HTTP action connection fetching by name completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

//    @PatchMapping("/connections/{name}")
//    public ResponseEntity<Response> updateConnectionByName(@PathVariable("name") String name,
//                                                           @Valid @RequestBody HTTPConnectionDTO payload) {
//        name = name.toUpperCase();
//        log.info("HTTP action connection updating by name request received - name={}, payload={}", name, payload);
//        validateAuthStrategyDto(payload);
//        HTTPConnectionDTO result = service.updateConnectionByName(name, payload);
//        Response response = Response.builder()
//                .data(result)
//                .message("Connection updated successfully")
//                .requestId(MDC.get("RID"))
//                .status(HttpStatus.OK)
//                .build();
//        log.info("HTTP action connection updating by name completed successfully");
//        return new ResponseEntity<>(response, response.getStatus());
//    }






    private void validateAuthStrategyDto(HTTPConnectionDTO dto) {
        // Validation for an API key
        if (dto.getStrategy().equals(AuthenticationStrategy.API_KEY)) {
            boolean headerKeyIsPresent = (dto.getApiKeyHeader() != null && !dto.getApiKeyHeader().trim().isBlank());
            boolean headerValueIsPresent = (dto.getApiKey() != null && !dto.getApiKey().trim().isBlank());
            boolean isValidConnectionData = (headerValueIsPresent && headerKeyIsPresent);
            log.debug("isValidConnectionData: {}, headerKeyIsPresent: {}, headerValueIsPresent:{}",
                    isValidConnectionData, headerKeyIsPresent, headerValueIsPresent);
            if (!isValidConnectionData) throw new InvalidConnectionException("Missing required fields (apiKey or apiKeyHeader)");
        }
        else if (dto.getStrategy().equals(AuthenticationStrategy.OAUTH2)) {
            if (dto.getGrantType() == null) throw new InvalidConnectionException("Missing required fields (grantType)");
            List<String> missing = new ArrayList<>();
            if (blank(dto.getTokenEndpoint())) missing.add("tokenEndpoint");

            switch (Objects.requireNonNull(dto.getGrantType())) {
                case CLIENT_CREDENTIALS -> {
                    if (blank(dto.getClientId()))     missing.add("clientId");
                    if (blank(dto.getClientSecret())) missing.add("clientSecret");
                }
                case PASSWORD -> {
                    if (blank(dto.getClientId()))  missing.add("clientId");
                    if (blank(dto.getUsername()))  missing.add("username");
                    if (blank(dto.getPassword()))  missing.add("password");
                }
                case AUTHORIZATION_CODE -> {
                    if (blank(dto.getClientId()))    missing.add("clientId");
                    if (blank(dto.getCode()))        missing.add("code");
                    if (blank(dto.getRedirectUri())) missing.add("redirectUri");
                }
            }

            if (!missing.isEmpty()) throw new InvalidConnectionException("Missing required fields for " + dto.getGrantType() + ": " + missing);
        }


    }

    private static boolean blank(String s){ return s == null || s.isBlank(); }

}
