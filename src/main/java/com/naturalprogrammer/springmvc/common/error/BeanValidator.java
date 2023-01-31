package com.naturalprogrammer.springmvc.common.error;

import io.jbock.util.Either;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

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

    public <T, R> Either<Problem, R> validateAndGet(
            T request,
            ProblemType problemType,
            Supplier<Either<Problem, R>> supplier) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        return violations.isEmpty()
                ? supplier.get()
                : Either.left(problemComposer.compose(problemType, request.toString(), violations));
    }

}
