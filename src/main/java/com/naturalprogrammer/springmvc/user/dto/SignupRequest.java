package com.naturalprogrammer.springmvc.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naturalprogrammer.springmvc.user.domain.MyUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @Email
        @NotBlank
        @Size(max = MyUser.EMAIL_MAX)
        @JsonProperty("email")
        String email,

        @NotBlank
        @Size(min = MyUser.PASSWORD_MIN, max = MyUser.PASSWORD_MAX)
        @JsonProperty("password")
        String password,

        @NotBlank
        @Size(min = MyUser.NAME_MIN, max = MyUser.NAME_MAX)
        @JsonProperty("displayName")
        String displayName
) {
        @Override
        public String toString() {
                return "SignupRequest{" +
                        "email='" + email + '\'' +
                        ", displayName='" + displayName + '\'' +
                        '}';
        }
}
