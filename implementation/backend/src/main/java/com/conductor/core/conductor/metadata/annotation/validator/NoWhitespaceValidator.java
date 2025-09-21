package com.conductor.core.conductor.metadata.annotation.validator;


import com.conductor.core.conductor.metadata.annotation.NoWhitespace;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoWhitespaceValidator implements ConstraintValidator<NoWhitespace, String> {
    @Override public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return value == null || !value.matches(".*\\s.*");
    }
}

