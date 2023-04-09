package com.naturalprogrammer.springmvc.user.features.forgot_password;

import com.naturalprogrammer.springmvc.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
record ForgotPasswordRequest(

        @Email
        @NotBlank
        @Size(max = User.EMAIL_MAX)
        @Schema(example = "sanjay@example.com")
        String email
) {

    public ForgotPasswordRequest trimmed() {
        var trimmed = new ForgotPasswordRequest(trim(email));
        log.info("Trimmed {} to {}", this, trimmed);
        return trimmed;
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "forgot-password-request.v1+json";
}
