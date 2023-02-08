package com.naturalprogrammer.springmvc.user.features.login;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record AccessTokenResource(
        String accessToken,
        String accessTokenValidTill
) {

    @Override
    public String toString() {
        return "AuthTokenResource{" +
                ", accessTokenValidTill='" + accessTokenValidTill + '\'' +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "access-token.v1+json";
}
