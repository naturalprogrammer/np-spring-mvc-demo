package com.naturalprogrammer.springmvc.user.domain;

import java.util.UUID;

public record UserId(UUID value) {
    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }
}
