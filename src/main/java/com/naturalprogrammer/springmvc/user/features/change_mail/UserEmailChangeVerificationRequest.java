package com.naturalprogrammer.springmvc.user.features.change_mail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

@Slf4j
record UserEmailChangeVerificationRequest(

        @NotBlank
        @Size(max = 5000)
        @Schema(example = "{token received via email}")
        String emailVerificationToken
) {
    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "user-email-change-verification-request.v1+json";
}
