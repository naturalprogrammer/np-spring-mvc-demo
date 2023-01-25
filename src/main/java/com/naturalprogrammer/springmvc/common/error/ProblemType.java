package com.naturalprogrammer.springmvc.common.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProblemType {

    INVALID_SIGNUP("/problems/invalid-signup", "invalid-signup", UNPROCESSABLE_ENTITY),
    USED_EMAIL("/problems/used-email", "used-email", HttpStatus.CONFLICT),
    GENERIC_ERROR("/problems/generic-error", "generic-error", HttpStatus.INTERNAL_SERVER_ERROR),
    HTTP_MESSAGE_NOT_READABLE("/problems/http-message-not-readable", "http-message-not-readable", HttpStatus.BAD_REQUEST),
    HTTP_MEDIA_TYPE_NOT_SUPPORTED("/problems/http-media-type-not-supported", "http-media-type-not-supported", HttpStatus.BAD_REQUEST);

    private final String type;
    private final String title;
    private final HttpStatus status;
}
