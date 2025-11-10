package com.vodafone.vcs.conductorwrapper.web.controller;

import com.vodafone.vcs.conductorwrapper.action.database.adapter.DatasourceAdapter;
import com.vodafone.vcs.conductorwrapper.action.database.dto.*;
import com.vodafone.vcs.conductorwrapper.action.database.entity.QueryStore;
import com.vodafone.vcs.conductorwrapper.action.database.service.DatabaseService;
import com.vodafone.vcs.conductorwrapper.common.response.Response;
import com.vodafone.vcs.conductorwrapper.conductor.service.ConductorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping(value = "/api/actions/database", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DatabaseController {

    private final DatasourceAdapter datasourceAdapter;
    private final DatabaseService databaseService;
    private final ConductorService conductorService;

    /* ********************************************** Datasource API ********************************************** */

    @PostMapping(value = "/datasources")
    public ResponseEntity<Response> registerDatasource(@Valid @RequestBody DatasourceDto payload) {
        log.info("Datasource registration request received - payload={}" ,payload);
        databaseService.saveAndRegisterDatasource(datasourceAdapter.toEntity(payload));
        URI location = URI.create(URI.create("/api/action/database/datasources/") + payload.getName().toUpperCase());

        Response response = Response.builder()
                .message("Datasource registered successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.CREATED)
                .build();

        log.info("Datasource registration request completed successfully");
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping(value = "/datasources")
    public ResponseEntity<Response> listAllDatasources(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Datasource list request received");

        Page<DatasourceDtoResponse> page = databaseService.listAllDatasources(pageable);
        Map<String, Object> data = Map.of(
                "items", page.getContent(),
                "page", page.getNumber(),
                "size", page.getSize(),
                "totalItems", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "sort", page.getSort().toString()
        );

        Response response = Response.builder()
                .message("Datasource fetched successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .data(data)
                .build();

        log.info("Datasource list request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PatchMapping(value = "/datasources/{name}")
    public ResponseEntity<Response> updateRegisteredDatasource(@PathVariable("name") String name, @Valid @RequestBody DatasourceDto datasourceDto) {
        log.info("Update datasource by name request received - name={}", name);
        DatasourceDtoResponse updated = databaseService.updateDatasourceByName(name, datasourceDto);
        Response response = Response.builder()
                .message("Datasource updated successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .data(updated)
                .build();
        log.info("Update datasource request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping(value = "/datasources/{name}")
    public ResponseEntity<Response> fetchDatasourceByName(@PathVariable("name") String name) {
        log.info("Fetch datasource by name request received - name={}", name);
        DatasourceDto fetched = databaseService.fetchDatasourceBuName(name);
        Response response = Response.builder()
                .message("Datasource fetched successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .data(fetched)
                .build();
        log.info("Fetch datasource request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @DeleteMapping(value = "/datasources/{name}")
    public ResponseEntity<Response> deleteDatasourceByName(@PathVariable("name") String name) {
        log.info("Delete datasource by name request received - name={}", name);
        databaseService.deleteDatasourceByName(name);
        Response response = Response.builder()
                .message("Datasource deleted successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.NO_CONTENT)
                .build();
        log.info("Delete datasource request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    /* ********************************************** Queries API ********************************************** */

    @PostMapping(value = "/queries")
    public ResponseEntity<Response> SaveQueryStore(@Valid @RequestBody QueryStoreDto payload) {
        log.info("Store query request received - payload={}" ,payload);

        QueryStoreDto result = databaseService.saveAndCacheQuery(payload);
        Response response = Response.builder()
                .message("Query stored successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.CREATED)
                .data(result)
                .build();

        URI location = URI.create(URI.create("/api/action/database/queries/") + payload.getName());
        log.info("Store query request completed successfully");
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping(value = "/queries/{name}")
    public ResponseEntity<Response> getQueryStoreByName(@PathVariable("name") String name) {
        log.info("Query fetch request received - name={}" ,name);
        QueryStoreDto result = databaseService.fetchQueryByName(name);
        Response response = Response.builder()
                .message("Query fetched successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .data(result)
                .build();
        log.info("Query fetch request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping(value = "/queries")
    public ResponseEntity<Response> listAllQueries(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Queries list request received");
        Page<QueryStore> page = databaseService.listAllQueries(pageable);

        Map<String, Object> data = Map.of(
                "items", page.getContent(),
                "page", page.getNumber(),
                "size", page.getSize(),
                "totalItems", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "sort", page.getSort().toString()
        );
        Response response = Response.builder()
                .message("Queries fetched successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .data(data)
                .build();

        log.info("Queries list request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PatchMapping("/queries/{name}")
    public ResponseEntity<Response> updateQueryStoreByName(@PathVariable("name") String name, @Valid @RequestBody QueryStoreDto payload) {
        log.info("Update query by name request received - name={}, payload={}", name, payload);
        QueryStoreDto updated = databaseService.updateQueryByName(name, payload);
        Response response = Response.builder()
                .message("Query updated successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .data(updated)
                .build();
        log.info("Update query request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @DeleteMapping("/queries/{name}")
    public ResponseEntity<Response> deleteQueryStoreByName(@PathVariable("name") String name) {
        log.info("Delete query by name request received - name={}", name);
        databaseService.deleteQueryByName(name);
        Response response = Response.builder()
                .message("Datasource deleted successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.NO_CONTENT)
                .build();
        log.info("Delete query request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

    /* ********************************************** Database workflow API ********************************************** */

    @PostMapping("/queries/execute")
    public ResponseEntity<Response> executeActionNode(@Valid @RequestBody Query payload) {
        log.info("Query execution request received - payload={}", payload);
        QueryResult result = databaseService.run(payload);
        Response response = Response.builder()
                .message("query executed successfully")
                .requestId(MDC.get("RID"))
                .status(HttpStatus.OK)
                .data(result)
                .build();
        log.info("Query execution request completed successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }

}
