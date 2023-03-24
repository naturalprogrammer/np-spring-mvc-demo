package com.naturalprogrammer.springmvc.user.features.login;

import java.time.Instant;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record AccessTokenResource(
        String accessToken,
        Instant accessTokenValidUntil
) {

    @Override
    public String toString() {
        return "ResourceTokenResource{" +
                ", accessTokenValidUntil='" + accessTokenValidUntil + '\'' +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "access-token.v1+json";
}
