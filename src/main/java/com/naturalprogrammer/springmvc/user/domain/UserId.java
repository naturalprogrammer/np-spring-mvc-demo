package com.naturalprogrammer.springmvc.user.domain;

import java.util.UUID;

public record UserId(UUID value) {

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String id) {
        return new UserId(UUID.fromString(id));
    }
}
