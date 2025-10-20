package com.vcs.conductor.metadata.annotation;

import com.vcs.conductor.metadata.annotation.validator.NoWhitespaceValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoWhitespaceValidator.class)
public @interface NoWhitespace {
    String message() default "Must not contain whitespace";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}