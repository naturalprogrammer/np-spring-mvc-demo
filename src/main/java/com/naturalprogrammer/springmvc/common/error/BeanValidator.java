package com.naturalprogrammer.springmvc.common.error;

import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BeanValidator {

    private final LocalValidatorFactoryBean validator;
    private final ProblemComposer problemComposer;

    public <R> Optional<Problem> validate(R request, ProblemType problemType) {
        Set<ConstraintViolation<R>> violations = validator.validate(request);
        return violations.isEmpty()
                ? Optional.empty()
                : Optional.of(problemComposer.compose(problemType, request.toString(), violations));
    }
}
