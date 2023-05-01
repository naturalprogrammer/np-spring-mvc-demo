package com.naturalprogrammer.springmvc.common.error;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Slf4j
@Component
@RequiredArgsConstructor
@Scope(value = SCOPE_PROTOTYPE)
public class ProblemBuilder {

    private final MessageGetter messageGetter;

    private String type;
    private String title;
    private int status;
    private String detail;
    private final List<Error> errors = new ArrayList<>();

    public ProblemBuilder type(ProblemType problemType) {
        type = problemType.getType();
        title = messageGetter.getMessage(problemType.getTitle());
        status = problemType.getStatus().value();
        return this;
    }

    public ProblemBuilder detail(String problemDetail) {
        detail = problemDetail;
        return this;
    }

    public ProblemBuilder detailMessage(String messageKey, Object... args) {
        detail = messageGetter.getMessage(messageKey, args);
        return this;
    }

    public ProblemBuilder error(String field, ErrorCode errorCode, Object... args) {
        var errorMessage = messageGetter.getMessage(errorCode.getMessage(), args);
        errors.add(new Error(errorCode.getCode(), errorMessage, field));
        return this;
    }

    public ProblemBuilder errors(Collection<Error> errors) {
        this.errors.addAll(errors);
        return this;
    }

    public Problem build() {
        var problem = new Problem(
                UUID.randomUUID().toString(),
                type,
                title,
                status,
                detail,
                null,
                errors
        );
        log.info("Faced {}", problem);
        return problem;
    }

    public Problem build(ProblemType type, String detail) {
        return type(type).detail(detail).build();
    }
}
