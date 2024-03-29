package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokensResource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record UserResource(

        @Schema(example = "8fd1502e-759d-419f-aaac-e61478fc6406")
        UUID id,

        @Schema(example = "sanjay@example.com")
        String email,

        @Schema(example = "Sanjay Patel")
        String displayName,

        @Schema(example = "en-IN")
        String locale,

        @Schema(example = "['USER', 'UNVERIFIED]")
        Set<Role> roles,

        @Schema(title = "Access and resource tokens for accessing the API. Optional")
        AuthTokensResource authTokens
) {
    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "user.v1+json";
        public static final String LIST_TYPE = CONTENT_TYPE_PREFIX + "users.v1+json";
}
