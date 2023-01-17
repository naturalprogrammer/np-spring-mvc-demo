package com.naturalprogrammer.springmvc.common.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProblemType {

    INVALID_SIGNUP("/problems/invalid-signup", "invalid-signup", UNPROCESSABLE_ENTITY);

    private final String type;
    private final String title;
    private final HttpStatus status;
}
