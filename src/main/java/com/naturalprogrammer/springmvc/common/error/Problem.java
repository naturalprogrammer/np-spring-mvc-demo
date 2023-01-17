package com.naturalprogrammer.springmvc.common.error;

import org.springframework.http.ResponseEntity;

import java.util.Set;

// https://www.rfc-editor.org/rfc/rfc7807
public record Problem(
        String id, // UUID
        String type, // e.g. /problems/invalid-signup
        String title, // e.g. "Invalid fields received while doing signup"
        int status, // e.g. 422
        String detail, // Maybe serialized JSON of the input, masking sensitive info
        String instance, // relative URI for more info of the instance
        Set<Error> errors
) {
    public static final String CONTENT_TYPE = "application/problem+json";

    public static ResponseEntity<Problem> toResponse(Problem problem) {
        return ResponseEntity
                .status(problem.status())
                .header(CONTENT_TYPE, Problem.CONTENT_TYPE)
                .body(problem);
    }
}
