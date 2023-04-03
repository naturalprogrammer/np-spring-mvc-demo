package com.naturalprogrammer.springmvc.common.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProblemType {

    GENERIC_ERROR("/problems/generic-error", "generic-error", INTERNAL_SERVER_ERROR),
    HTTP_MESSAGE_NOT_READABLE("/problems/http-message-not-readable", "http-message-not-readable", BAD_REQUEST),
    HTTP_MEDIA_TYPE_NOT_SUPPORTED("/problems/http-media-type-not-supported", "http-media-type-not-supported", BAD_REQUEST),
    INVALID_DATA("/problems/invalid-data", "invalid-data", UNPROCESSABLE_ENTITY),
    USED_EMAIL("/problems/used-email", "used-email", CONFLICT),
    TOKEN_VERIFICATION_FAILED("/problems/token-verification-failed", "token-verification-failed", FORBIDDEN),
    WRONG_JWT_AUDIENCE("/problems/wrong-jwt-audience", "wrong-jwt-audience", FORBIDDEN),
    EXPIRED_JWT("/problems/expired-jwt", "expired-jwt", FORBIDDEN),
    WRONG_CREDENTIALS("/problems/wrong-credentials", "wrong-credentials", UNAUTHORIZED),
    NOT_FOUND("/problems/not-found", "not-found", HttpStatus.NOT_FOUND);

    private final String type;
    private final String title;
    private final HttpStatus status;
}
