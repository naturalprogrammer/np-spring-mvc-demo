package com.naturalprogrammer.springmvc.common.jwt;

import java.util.Objects;

public record Subject(String value) {
    public Subject {
        Objects.requireNonNull(value);
    }
}
