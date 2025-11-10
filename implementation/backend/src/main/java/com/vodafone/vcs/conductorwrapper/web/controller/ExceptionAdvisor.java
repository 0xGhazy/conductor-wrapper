package com.vodafone.vcs.conductorwrapper.web.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vodafone.vcs.conductorwrapper.action.database.exception.DatasourceDuplicationException;
import com.vodafone.vcs.conductorwrapper.action.database.exception.DatasourceNotFoundException;
import com.vodafone.vcs.conductorwrapper.action.database.exception.GenericErrorException;
import com.vodafone.vcs.conductorwrapper.common.response.ResponseError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Log4j2
@ControllerAdvice
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionAdvisor extends ResponseEntityExceptionHandler {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";

        String message = "Invalid parameter";
        String reason = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", value, name, type);
        ResponseEntity<ResponseError> response = buildErrorResponse(message, reason, null, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DatasourceDuplicationException.class)
    public ResponseEntity<ResponseError> handleDatasourceDuplicationException(DatasourceDuplicationException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ResponseError error = ResponseError.builder()
                .message(ex.getMessage())
                .details(path)
                .requestId(MDC.get("requestId"))
                .build();
        logResponseErrorDetails(error);
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(GenericErrorException.class)
    public ResponseEntity<ResponseError> handleGenericErrorException(GenericErrorException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ResponseError error = ResponseError.builder()
                .message(ex.getMessage())
                .details(path)
                .requestId(MDC.get("requestId"))
                .build();
        logResponseErrorDetails(error);
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(DatasourceNotFoundException.class)
    public ResponseEntity<ResponseError> handleDatasourceNotFoundException(DatasourceNotFoundException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ResponseError error = ResponseError.builder()
                .message(ex.getMessage())
                .details(path)
                .requestId(MDC.get("requestId"))
                .build();
        logResponseErrorDetails(error);
        return new ResponseEntity<>(error, ex.getStatus());
    }






//    @Override
//    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
//                                                                            @Nullable HttpHeaders headers,
//                                                                            @Nullable HttpStatusCode status,
//                                                                            WebRequest request) {
//
//        String requestId = MDC.get("requestId");
//        String source = ex.getMethod().getDeclaringClass().getSimpleName() + "." + ex.getMethod().getName();
//        String requestUri = request.getDescription(false).replace("uri=", "");
//        log.warn("requestId={}, source={}, requestUri={}, requestParams={}", requestId, source, requestUri, request.getParameterMap());
//
//        List<ValidationResponseError> errorList = ex.getAllErrors().stream().map(error -> {
//            String defaultMessage = Optional.ofNullable(error.getDefaultMessage()).orElse("UnknownField::Validation error");
//            String[] fieldWithMessage = defaultMessage.split("::");
//
//            String field = fieldWithMessage.length > 0 ? fieldWithMessage[0] : "UnknownField";
//            String message = fieldWithMessage.length > 1 ? fieldWithMessage[1] : "Validation error";
//
//            ValidationResponseError fieldError = new ValidationResponseError(field, message);
//            log.warn("requestId={}, Validation failure: field={}, message={}", requestId, field, message);
//            return fieldError;
//        }).collect(Collectors.toList());
//
//        ResponseError response = ResponseError.builder()
//                .message("Validation failure")
//                .details(errorList)
//                .timestamp(Instant.now())
//                .requestId(requestId)
//                .build();
//        logResponseErrorDetails(response);
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }


//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(
//            MethodArgumentNotValidException ex,
//            @Nullable HttpHeaders headers,
//            @Nullable HttpStatusCode status,
//            WebRequest request) {
//
//        List<HibernateValidationException> errors = new ArrayList<>();
//
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String field = ((FieldError) error).getField();
//            String message = error.getDefaultMessage();
//            errors.add(new HibernateValidationException(field, message));
//        });
//
//        ResponseError response = ResponseError.builder()
//                .message(String.format("Validation error in %s", errors.get(0).getField()))
//                .reason(errors.get(0).getReason())
//                .details(request.getContextPath().isEmpty() ? null: request.getContextPath())
//                .requestId(MDC.get("requestId"))
//                .build();
//        logResponseErrorDetails(response);
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }




    private void logResponseErrorDetails(ResponseError error) {
        log.error("Request failed,  reason={}, details={}, message={}",
                error.getReason(),
                error.getDetails(),
                error.getMessage()
        );
    }

    private ResponseEntity<ResponseError> buildErrorResponse(
            String message,
            String reason,
            Object details,
            HttpStatus status) {
        ResponseError response = ResponseError.builder()
                .message(message)
                .reason(reason)
                .details(details)
                .requestId(MDC.get("requestId"))
                .timestamp(Instant.now())
                .build();
        logResponseErrorDetails(response);
        return ResponseEntity.status(status).body(response);
    }


}
