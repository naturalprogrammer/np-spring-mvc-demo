package com.naturalprogrammer.springmvc.common.error;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProblemComposer {

    private final MessageGetter messageGetter;


    public Problem compose(
            ProblemType type,
            String detail,
            ErrorCode error,
            String field
    ) {
        return compose(type, detail, List.of(
                getError(error.getCode(), error.getMessage(), field)
        ));
    }

    public Error getError(String code, String messageKey, String field) {
        return new Error(
                code,
                messageGetter.getMessage(messageKey),
                field
        );
    }

    public Problem compose(
            ProblemType type,
            String detail,
            List<Error> errors
    ) {
        return new Problem(
                UUID.randomUUID().toString(),
                type.getType(),
                messageGetter.getMessage(type.getTitle()),
                type.getStatus().value(),
                detail,
                null,
                errors
        );
    }

    public <T> Problem ofViolations(
            ProblemType type,
            String detail,
            Set<ConstraintViolation<T>> violations
    ) {
        return compose(type, detail, toErrors(violations));
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
