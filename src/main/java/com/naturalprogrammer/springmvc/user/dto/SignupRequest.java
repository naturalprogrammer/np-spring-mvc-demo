package com.naturalprogrammer.springmvc.user.dto;

import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record SignupRequest(

        @Email
        @NotBlank
        @Size(max = User.EMAIL_MAX)
        @Schema(example = "sanjay@example.com")
        String email,

        @ValidPassword
        @Schema(example = "Secret99!", description = "Password must have least 1 upper, lower, special characters and digit, min 8 chars, max 50 chars")
        String password,

        @NotBlank
        @Size(min = User.NAME_MIN, max = User.NAME_MAX)
        @Schema(example = "Sanjay Patel")
        String displayName
) {
    @Override
    public String toString() {
        return "SignupRequest{" +
                "email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "signup-request.v1+json";
}
