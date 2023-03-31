package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

record LoginRequest(

        @Email
        @Schema(example = "sanjay@example.com")
        String email,

        @ValidPassword
        @Schema(example = "Secret99!")
        String password,

        @Schema(example = "33425233")
        Long resourceTokenValidForMillis

) {
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", resourceTokenValidForMillis=" + resourceTokenValidForMillis +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "login-request.v1+json";
}
