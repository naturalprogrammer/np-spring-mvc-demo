package com.naturalprogrammer.springmvc.user.features.login;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthScope {

    NORMAL("normal"),
    AUTH_TOKENS("auth_tokens"),
    EXCHANGE_RESOURCE_TOKEN("exchange_resource_token");

    private final String value;

    public final String scope() {
        return "SCOPE_" + value;
    }
}
