package com.naturalprogrammer.springmvc.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResource(

        @Schema(example = "8fd1502e-759d-419f-aaac-e61478fc6406")
        String id,

        @Schema(example = "sanjay@example.com")
        String email,

        @Schema(example = "Sanjay Patel")
        String displayName,

        @Schema(example = "en-IN")
        String locale,

        @Schema(
                title = "Optional bearer access-token for accessing the API next time",
                example = "jwt"
        )
        String token
) {
    public static final String CONTENT_TYPE = "application/vnd.com.naturalprogrammer.user.v1+json";
}
