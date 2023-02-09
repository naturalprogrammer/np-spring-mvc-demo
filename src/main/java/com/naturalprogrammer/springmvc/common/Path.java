package com.naturalprogrammer.springmvc.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Path {

    public static final String USERS = "/users";
    public static final String AUTH_TOKENS = "/auth-tokens";
}
