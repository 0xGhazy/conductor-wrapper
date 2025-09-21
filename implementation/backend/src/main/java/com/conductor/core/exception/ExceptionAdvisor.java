package com.conductor.core.exception;


import com.conductor.core.dto.ResponseError;
import com.conductor.core.dto.ValidationResponseError;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Log4j2
@ControllerAdvice
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionAdvisor extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {

        String trxId = MDC.get("trxId");
        String source = ex.getMethod().getDeclaringClass().getSimpleName() + "." + ex.getMethod().getName();
        String requestUri = request.getDescription(false).replace("uri=", "");
        log.warn("trxId={}, source={}, requestUri={}, requestParams={}", trxId, source, requestUri, request.getParameterMap());

        List<ValidationResponseError> errorList = ex.getAllErrors().stream().map(error -> {
            String defaultMessage = Optional.ofNullable(error.getDefaultMessage()).orElse("UnknownField::Validation error");
            String[] fieldWithMessage = defaultMessage.split("::");

            String field = fieldWithMessage.length > 0 ? fieldWithMessage[0] : "UnknownField";
            String message = fieldWithMessage.length > 1 ? fieldWithMessage[1] : "Validation error";

            ValidationResponseError fieldError = new ValidationResponseError(field, message);
            log.warn("trxId={}, Validation failure: field={}, message={}", trxId, field, message);
            return fieldError;
        }).collect(Collectors.toList());

        ResponseError response = ResponseError.builder()
                .message("Validation failure")
                .details(errorList)
                .timestamp(LocalDateTime.now())
                .trxId(trxId)
                .build();

        logException(response);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<HibernateValidationException> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.add(new HibernateValidationException(field, message));
        });

        ResponseError response = ResponseError.builder()
                .message(String.format("Validation error in %s", errors.get(0).getField()))
                .reason(errors.get(0).getReason())
                .details(request.getContextPath().isEmpty() ? null: request.getContextPath())
                .build();

        logException(response);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String trxId = MDC.get("trxId");
        ResponseError response = ResponseError.builder()
                .message("Request body is missing or invalid")
                .trxId(trxId)
                .timestamp(LocalDateTime.now())
                .build();

        logException(response);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({ InternalServerError.class })
    public ResponseEntity<?> handleInternalSystemErrorException(InternalServerError ex, WebRequest webRequest) {
        String trxId = MDC.get("trxId");
        ResponseError response = ResponseError.builder()
                .message(ex.getMessage())
                .reason(ex.getReason())
                .details(webRequest.getContextPath())
                .trxId(trxId)
                .build();
        logException(response);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler({ BadRequest.class })
    public ResponseEntity<?> handleBadRequestException(BadRequest ex, WebRequest webRequest) {
        String trxId = MDC.get("trxId");
        ResponseError response = ResponseError.builder()
                .message(ex.getMessage())
                .reason(ex.getReason())
                .build();
        logException(response);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler({ Ineligible.class })
    public ResponseEntity<?> handleIneligibleException(Ineligible ex, WebRequest webRequest) {
        String trxId = MDC.get("trxId");
        ResponseError response = ResponseError.builder()
                .message(ex.getMessage())
                .reason(ex.getReason())
                .build();
        logException(response);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler({ NotFound.class })
    public ResponseEntity<?> handleNotFoundException(NotFound ex, WebRequest webRequest) {
        String trxId = MDC.get("trxId");
        ResponseError response = ResponseError.builder()
                .message(ex.getMessage())
                .reason(ex.getReason())
                .trxId(trxId)
                .build();
        logException(response);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    private void logException(ResponseError response) {
        log.error("reason={}, details={}, message={}", response.getReason(), response.getDetails(), response.getMessage());
    }


}
