package com.naturalprogrammer.springmvc.common.jwt;

import java.util.Objects;

public record Token(String value) {
    public Token {
        Objects.requireNonNull(value);
    }
}
