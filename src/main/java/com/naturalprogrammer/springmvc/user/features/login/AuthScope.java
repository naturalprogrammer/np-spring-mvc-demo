package com.naturalprogrammer.springmvc.user.features.login;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthScope {

    RESOURCE_TOKEN("resource_token");

    private final String value;

    public final String authority() {
        return "SCOPE_" + value;
    }
}
