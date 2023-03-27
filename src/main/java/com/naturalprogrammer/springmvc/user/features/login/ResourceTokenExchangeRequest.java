package com.naturalprogrammer.springmvc.user.features.login;

import jakarta.validation.constraints.NotBlank;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

record ResourceTokenExchangeRequest(

        @NotBlank
        String myClientId,

        Long resourceTokenValidForMillis
) {
    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "resource-token-exchange-request.v1+json";

    @Override
    public String toString() {
        return "ResourceTokenExchangeRequest{" +
                "resourceTokenValidForMillis=" + resourceTokenValidForMillis +
                '}';
    }
}
