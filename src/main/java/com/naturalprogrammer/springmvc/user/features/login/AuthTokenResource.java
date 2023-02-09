package com.naturalprogrammer.springmvc.user.features.login;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record AuthTokenResource(

        @Schema(
                title = "Optional bearer access-token for accessing the API next time",
                example = "jwt with 'resource-token' scope"
        )
        String resourceToken,

        @Schema(
                title = "Optional bearer access-token for accessing the API next time",
                example = "jwt"
        )
        String accessToken,

        @Schema(
                title = "Till when the resource token is valid",
                example = "2029-12-29T12:13:56Z"
        )
        Instant resourceTokenValidUntil,

        @Schema(
                title = "Till when the access token is valid",
                example = "2028-11-28T11:44:51Z"
        )
        Instant accessTokenValidUntil
) {

    @Override
    public String toString() {
        return "AuthTokenResource{" +
                "resourceTokenValidUntil='" + resourceTokenValidUntil + '\'' +
                ", accessTokenValidUntil='" + accessTokenValidUntil + '\'' +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "auth-token.v1+json";
}
