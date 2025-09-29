package com.vcs.flowpilot.action.database.internal.controller;

import com.vcs.flowpilot.action.database.internal.dto.DatasourceDto;
import com.vcs.flowpilot.action.database.internal.dto.ResponseDatasourceDto;
import com.vcs.flowpilot.action.database.internal.service.DatasourceService;
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
@RequestMapping("/api/action/db")
@RequiredArgsConstructor
public class DatabaseActionController {

    private final DatasourceService datasourceService;

    /* ********************************************** Datasource API ********************************************** */

    @PostMapping(value = "/datasource/cache/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ResponseDatasourceDto>> refreshCache() {
        log.info("Datasource cache refresh request received");
        long start = System.currentTimeMillis();
        List<ResponseDatasourceDto> result = datasourceService.cacheRefresh();
        long duration = (System.currentTimeMillis() - start);
        log.info("Datasource cache refresh request completed successfully - duration {}ms", duration);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDatasourceDto> registerDatasource(@Valid @RequestBody DatasourceDto payload) {
        log.info("Datasource registration request received - payload = {}" ,payload);
        long start = System.currentTimeMillis();
        ResponseDatasourceDto registeredDs = datasourceService.saveAndRegisterDatasource(payload);
        URI location = URI.create("/api/action/db/" + registeredDs.getName());
        long duration = (System.currentTimeMillis() - start);
        log.info("Datasource registration request completed successfully - duration {}ms", duration);
        return ResponseEntity.created(location).body(registeredDs);
    }

    @GetMapping(value = "/datasource/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDatasourceDto> fetchRegisteredDatasource(@PathVariable("name") String name) {
        log.info("Fetch datasource by name request received - name = {}" ,name);
        long start = System.currentTimeMillis();
        ResponseDatasourceDto registeredDs = datasourceService.fetchDatasourceByName(name);
        long duration = (System.currentTimeMillis() - start);
        log.info("Datasource fetching request completed successfully - duration {}ms", duration);
        return ResponseEntity.ok(registeredDs);
    }

    @GetMapping(value = "/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ResponseDatasourceDto>> fetchAllRegisteredDatasource() {
        log.info("Fetch all data sources request received");
        long start = System.currentTimeMillis();
        List<ResponseDatasourceDto> fetchedAllDataSources = datasourceService.fetchAllDataSources();
        long duration = (System.currentTimeMillis() - start);
        log.info("Datasource fetching all request completed successfully - duration {}ms", duration);
        return ResponseEntity.ok(fetchedAllDataSources);
    }

    @PatchMapping(value = "/datasource/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDatasourceDto> updateRegisteredDatasource(@PathVariable("name") String name,
                                                                            @Valid @RequestBody DatasourceDto datasourceDto) {
        log.info("Update data sources by name request received - name = {}", name);
        long start = System.currentTimeMillis();
        ResponseDatasourceDto updated = datasourceService.updateDatasourceByName(name, datasourceDto);
        long duration = (System.currentTimeMillis() - start);
        log.info("Update datasource request completed successfully - duration {}ms", duration);
        return ResponseEntity.ok(updated);
    }

    /* ********************************************** Query API ********************************************** */

}
