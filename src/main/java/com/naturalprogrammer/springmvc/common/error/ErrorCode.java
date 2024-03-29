package com.naturalprogrammer.springmvc.common.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

    USED_EMAIL("UsedEmail", ProblemType.USED_EMAIL.getTitle()),
    TOKEN_VERIFICATION_FAILED("TokenVerificationFailed", ProblemType.TOKEN_VERIFICATION_FAILED.getTitle()),
    PASSWORD_MISMATCH("PasswordMismatch", ProblemType.PASSWORD_MISMATCH.getTitle()),
    EMAIL_MISMATCH("EmailMismatch", ProblemType.EMAIL_MISMATCH.getTitle());

    private final String code;
    private final String message;
}
