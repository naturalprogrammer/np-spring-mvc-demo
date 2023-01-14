package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.Column;

import java.util.UUID;

public record UserId(@Column UUID id) {
}
