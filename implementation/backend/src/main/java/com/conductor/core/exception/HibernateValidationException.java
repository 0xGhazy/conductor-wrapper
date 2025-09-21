package com.conductor.core.exception;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NotBlank
public class HibernateValidationException extends RuntimeException{
    private String field;
    private String reason;
}
