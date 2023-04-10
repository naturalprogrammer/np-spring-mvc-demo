package com.naturalprogrammer.springmvc.user.features.reset_password;

import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;
import static com.naturalprogrammer.springmvc.user.validators.PasswordValidator.PASSWORD_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
record ResetPasswordRequest(

        @NotBlank
        @Size(max = 4096)
        @Schema(example = "JWE token")
        String token,

        @ValidPassword
        @Schema(example = "Secret99!", description = PASSWORD_DESCRIPTION)
        String newPassword

) {

    public ResetPasswordRequest trimmed() {
        var trimmed = new ResetPasswordRequest(
                trim(token), trim(newPassword)
        );
        log.info("Trimmed {} to {}", this, trimmed);
        return trimmed;
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "token='" + token + '\'' +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "reset-password-request.v1+json";

}
