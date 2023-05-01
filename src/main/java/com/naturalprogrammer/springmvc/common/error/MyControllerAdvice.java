package com.naturalprogrammer.springmvc.common.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

import static com.naturalprogrammer.springmvc.common.error.Problem.toResponse;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class MyControllerAdvice {

    private final ObjectFactory<ProblemBuilder> problemBuilder;

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Problem> handleException(HttpMediaTypeNotSupportedException ex) {

        var problem = problemBuilder.getObject().build(ProblemType.HTTP_MEDIA_TYPE_NOT_SUPPORTED, ex.getMessage());
        log.info("HttpMediaTypeNotSupportedException (%s): %s".formatted(ex.getMessage(), problem), ex);
        return toResponse(problem);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> handleException(HttpMessageNotReadableException ex) {

        var problem = problemBuilder.getObject().build(ProblemType.HTTP_MESSAGE_NOT_READABLE, ex.getMessage());
        log.info("HttpMessageNotReadableException (%s): %s".formatted(ex.getMessage(), problem), ex);
        return toResponse(problem);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Problem> handleException(Exception ex) throws Exception {

        if (isBetterHandledBySpring(ex)) {
            log.warn("Spring exception", ex);
            throw ex;
        }

        var problem = problemBuilder.getObject().build(ProblemType.GENERIC_ERROR, null);
        log.error("Unknown error %s (%s): %s".formatted(ex.getClass().getCanonicalName(), ex.getMessage(), problem), ex);
        return toResponse(problem);
    }

    private boolean isBetterHandledBySpring(Exception ex) {
        return ex instanceof AccessDeniedException;
    }
}
