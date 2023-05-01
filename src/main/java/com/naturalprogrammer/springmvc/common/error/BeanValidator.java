package com.naturalprogrammer.springmvc.common.error;

import io.jbock.util.Either;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;

@Component
@RequiredArgsConstructor
public class BeanValidator {

    private final LocalValidatorFactoryBean validator;
    private final ObjectFactory<ProblemBuilder> problemBuilder;

    public <R> Optional<Problem> validate(R request) {
        Set<ConstraintViolation<R>> violations = validator.validate(request);
        return violations.isEmpty() ? Optional.empty() : Optional.of(
                problemBuilder.getObject()
                        .type(INVALID_DATA)
                        .detail(request.toString())
                        .errors(toErrors(violations))
                        .build());
    }

    public <T, R> Either<Problem, R> validateAndGet(T request, Supplier<Either<Problem, R>> supplier) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        return violations.isEmpty() ? supplier.get() : Either.left(
                problemBuilder.getObject()
                        .type(INVALID_DATA)
                        .detail(request.toString())
                        .errors(toErrors(violations))
                        .build());
    }

    private <T> List<Error> toErrors(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(this::toError).toList();
    }

    private Error toError(ConstraintViolation<?> violation) {
        return new Error(
                toCode(violation.getMessageTemplate()),
                violation.getMessage(),
                violation.getPropertyPath().toString());
    }

    private String toCode(String messageTemplate) {

        // Extracts "NotBlank" from "{jakarta.validation.constraints.NotBlank.message}"

        int lastPeriodIndex = messageTemplate.lastIndexOf(".");
        int secondLastPeriodIndex = messageTemplate.lastIndexOf(".", lastPeriodIndex - 1);
        return messageTemplate.substring(secondLastPeriodIndex + 1, lastPeriodIndex);
    }

}
