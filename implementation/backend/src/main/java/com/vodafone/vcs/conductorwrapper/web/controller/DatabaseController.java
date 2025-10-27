package com.vodafone.vcs.conductorwrapper.web.controller;

import com.vodafone.vcs.conductorwrapper.action.database.service.DatasourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/conductor/admin/db")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatasourceService datasourceService;

    /* ********************************************** Datasource API ********************************************** */

//    @PostMapping(value = "/datasource/cache/refresh", headers = "X-API-VERSION=1", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<DatasourceDtoResponse>> refreshCache() {
//        log.info("Datasource cache refresh request received");
//        long start = System.currentTimeMillis();
//        List<DatasourceDtoResponse> result = datasourceService.cacheRefresh();
//        long duration = (System.currentTimeMillis() - start);
//        log.info("Datasource cache refresh request completed successfully - duration {}ms", duration);
//        return ResponseEntity.ok(result);
//    }
//
//    @PostMapping(value = "/datasource", headers = "X-API-VERSION=1", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<DatasourceDtoResponse> registerDatasource(@Valid @RequestBody DatasourceDto payload) {
//        log.info("Datasource registration request received - payload = {}" ,payload);
//        long start = System.currentTimeMillis();
//        DatasourceDtoResponse registeredDs = datasourceService.saveAndRegisterDatasource(payload);
//        URI location = URI.create("/api/action/db/" + registeredDs.name());
//        long duration = (System.currentTimeMillis() - start);
//        log.info("Datasource registration request completed successfully - duration {}ms", duration);
//        return ResponseEntity.created(location).body(registeredDs);
//    }
//
//    @GetMapping(value = "/datasource/{name}", headers = "X-API-VERSION=1", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<DatasourceDtoResponse> fetchRegisteredDatasource(@PathVariable("name") String name) {
//        log.info("Fetch datasource by name request received - name = {}" ,name);
//        long start = System.currentTimeMillis();
//        DatasourceDtoResponse registeredDs = datasourceService.fetchDatasourceByName(name);
//        long duration = (System.currentTimeMillis() - start);
//        log.info("Datasource fetching request completed successfully - duration {}ms", duration);
//        return ResponseEntity.ok(registeredDs);
//    }
//
//    @GetMapping(value = "/datasource", headers = "X-API-VERSION=1", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<DatasourceDtoResponse>> fetchAllRegisteredDatasource() {
//        log.info("Fetch all data sources request received");
//        long start = System.currentTimeMillis();
//        List<DatasourceDtoResponse> fetchedAllDataSources = datasourceService.fetchAllDataSources();
//        long duration = (System.currentTimeMillis() - start);
//        log.info("Datasource fetching all request completed successfully - duration {}ms", duration);
//        return ResponseEntity.ok(fetchedAllDataSources);
//    }
//
//    @PatchMapping(value = "/datasource/{name}", headers = "X-API-VERSION=1", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<DatasourceDtoResponse> updateRegisteredDatasource(@PathVariable("name") String name,
//                                                                            @Valid @RequestBody DatasourceDto datasourceDto) {
//        log.info("Update data sources by name request received - name = {}", name);
//        long start = System.currentTimeMillis();
//        DatasourceDtoResponse updated = datasourceService.updateDatasourceByName(name, datasourceDto);
//        long duration = (System.currentTimeMillis() - start);
//        log.info("Update datasource request completed successfully - duration {}ms", duration);
//        return ResponseEntity.ok(updated);
//    }

    /* ********************************************** Query API ********************************************** */

}
