package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;
import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.RESOURCE_TOKEN_VALID_MILLIS_DESCR;
import static com.naturalprogrammer.springmvc.user.validators.PasswordValidator.PASSWORD_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
record LoginRequest(

        @NotBlank
        @Email
        @Schema(example = "sanjay@example.com")
        String email,

        @ValidPassword
        @Schema(example = "Secret99!", description = PASSWORD_DESCRIPTION)
        String password,

        @Schema(example = "1209600000", description = RESOURCE_TOKEN_VALID_MILLIS_DESCR)
        Long resourceTokenValidForMillis

) {

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "login-request.v1+json";

    public LoginRequest trimmed() {
        var trimmed = new LoginRequest(
                trim(email), trim(password), resourceTokenValidForMillis
        );
        log.info("Trimmed {} to  {}", this, trimmed);
        return trimmed;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", resourceTokenValidForMillis=" + resourceTokenValidForMillis +
                '}';
    }
}
