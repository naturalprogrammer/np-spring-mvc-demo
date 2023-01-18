package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UserIdClass {

    @Column(nullable = false, updatable = false)
    @Convert(converter = UserIdConverter.class)
    private UserId id;
}
