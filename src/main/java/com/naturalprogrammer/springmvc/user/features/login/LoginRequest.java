package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import jakarta.validation.constraints.Email;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record LoginRequest(

        @Email
        String email,

        @ValidPassword
        String password,

        Long resourceTokenValidForMillis,
        Long accessTokenValidForMillis

) {
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", resourceTokenValidForMillis=" + resourceTokenValidForMillis +
                ", accessTokenValidForMillis=" + accessTokenValidForMillis +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "login-request.v1+json";
}
