package com.naturalprogrammer.springmvc.common.jwt;

import java.util.Objects;

public record Aud(String value) {
    public Aud {
        Objects.requireNonNull(value);
    }
}
