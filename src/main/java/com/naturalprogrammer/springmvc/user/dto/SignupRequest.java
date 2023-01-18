package com.naturalprogrammer.springmvc.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naturalprogrammer.springmvc.user.domain.MyUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @Email
        @NotBlank
        @Size(max = MyUser.EMAIL_MAX)
        @JsonProperty("email")
        @Schema(example = "sanjay@example.com")
        String email,

        @NotBlank
        @Size(min = MyUser.PASSWORD_MIN, max = MyUser.PASSWORD_MAX)
        @JsonProperty("password")
        @Schema(example = "Secret99!")
        String password,

        @NotBlank
        @Size(min = MyUser.NAME_MIN, max = MyUser.NAME_MAX)
        @JsonProperty("displayName")
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

    public static final String CONTENT_TYPE = "application/vnd.com.naturalprogrammer.signup-request.v1+json";
}
