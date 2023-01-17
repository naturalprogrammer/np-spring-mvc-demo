package com.naturalprogrammer.springmvc.common.error;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProblemComposer {

    private final MessageGetter messageGetter;

    public <T> Problem compose(
            ProblemType type,
            String detail,
            Set<ConstraintViolation<T>> violations
    ) {
        return new Problem(
                UUID.randomUUID().toString(),
                type.getType(),
                messageGetter.getMessage(type.getTitle()),
                type.getStatus().value(),
                detail,
                null,
                toErrors(violations)
        );
    }

    private <T> Set<Error> toErrors(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(this::toError).collect(Collectors.toSet());
    }

    private Error toError(ConstraintViolation<?> violation) {
        return new Error(
                violation.getMessageTemplate(),
                violation.getMessage(),
                violation.getPropertyPath().toString());
    }
}
