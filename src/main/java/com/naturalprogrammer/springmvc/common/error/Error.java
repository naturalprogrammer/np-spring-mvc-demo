package com.naturalprogrammer.springmvc.common.error;

public record Error(
        String code,
        String message,
        String field
) {
}
