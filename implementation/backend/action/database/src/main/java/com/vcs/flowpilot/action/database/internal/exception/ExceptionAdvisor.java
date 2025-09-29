package com.vcs.flowpilot.action.database.internal.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class ExceptionAdvisor {

    @ExceptionHandler(DatasourceDuplicationException.class)
    public ResponseEntity<?> handleDatasourceDuplicationException(DatasourceDuplicationException e) {
        return ResponseEntity.ok("Datasource Duplication cached");
    }

}
