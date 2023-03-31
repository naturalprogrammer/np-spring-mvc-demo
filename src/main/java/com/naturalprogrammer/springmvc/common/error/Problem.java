package com.naturalprogrammer.springmvc.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

// https://www.rfc-editor.org/rfc/rfc7807
public record Problem(

        @Schema(title = "a unique identifier (UUID)", example = "aa6e97d9-9069-4051-8f08-16d066096c0f")
        String id,

        @Schema(title = "URL to the problem", example = "/problems/invalid-signup")
        String type,

        @Schema(title = "General description of the problem", example = "Invalid fields received while doing signup")
        String title,

        @Schema(example = "422")
        int status,

        @Schema(
                title = "Specific description of the problem",
                description = "Instance specific description of the problem, e.g. input DTO's toString()",
                example = "SignupRequest{email='null', displayName='null'}"
        )
        String detail,

        @Schema(
                title = "Relative URI for more info of the instance",
                example = "null"
        )
        String instance,

        List<Error> errors
) {
    public static final String CONTENT_TYPE = "application/problem+json";

    public static ResponseEntity<Problem> toResponse(Problem problem) {
        return ResponseEntity
                .status(problem.status())
                .header(HttpHeaders.CONTENT_TYPE, Problem.CONTENT_TYPE)
                .body(problem);
    }
}
