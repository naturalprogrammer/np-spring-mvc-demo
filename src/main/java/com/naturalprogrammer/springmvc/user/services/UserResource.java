package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record UserResource(

        @Schema(example = "8fd1502e-759d-419f-aaac-e61478fc6406")
        String id,

        @Schema(example = "sanjay@example.com")
        String email,

        @Schema(example = "Sanjay Patel")
        String displayName,

        @Schema(example = "en-IN")
        String locale,

        @Schema(example = "['USER', 'UNVERIFIED]")
        Set<Role> roles,

        @Schema(
                title = "Optional bearer access-token for accessing the API next time",
                example = "jwt"
        )
        String token
) {
    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "user.v1+json";

    @Override
    public String toString() {
        return "UserResource{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", locale='" + locale + '\'' +
                '}';
    }
}
