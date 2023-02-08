package com.naturalprogrammer.springmvc.user.features.login;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record AuthTokenResource(
        String resourceToken,
        String accessToken,
        String resourceTokenValidTill,
        String accessTokenValidTill
) {

    @Override
    public String toString() {
        return "AuthTokenResource{" +
                "resourceTokenValidTill='" + resourceTokenValidTill + '\'' +
                ", accessTokenValidTill='" + accessTokenValidTill + '\'' +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "auth-token.v1+json";
}
