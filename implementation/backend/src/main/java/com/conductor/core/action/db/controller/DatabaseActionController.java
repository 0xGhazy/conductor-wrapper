package com.conductor.core.action.db.controller;

import com.conductor.core.action.db.dto.Query;
import com.conductor.core.action.db.dto.QueryResult;
import com.conductor.core.action.db.service.DatabaseActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/action/database")
@RequiredArgsConstructor
public class DatabaseActionController {

    private final DatabaseActionService service;

    @GetMapping("/cache/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        service.evictAll();
        return ResponseEntity.ok(service.getCache());
    }

    @PostMapping("/{queryId}")
    public ResponseEntity<QueryResult> execute(@PathVariable("queryId") String queryId, @RequestBody Map<String, Object> params) {
        Query query = new Query.builder()
                .id(queryId)
                .parameters(params)
                .build();
        QueryResult result = service.run(query);
        return ResponseEntity.ok(result);
    }

}
