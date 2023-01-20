package com.naturalprogrammer.springmvc.user.validators;

import jakarta.validation.Constraint;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {

    String message() default "{com.naturalprogrammer.spring.invalid.password}";

    Class[] groups() default {};

    Class[] payload() default {};
}
