package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import jakarta.validation.constraints.Email;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

record AuthTokenRequest(

        @Email
        String email,

        @ValidPassword
        String password,

        Long resourceTokenValidForMillis

) {
    @Override
    public String toString() {
        return "AuthTokenRequest{" +
                "email='" + email + '\'' +
                ", resourceTokenValidForMillis=" + resourceTokenValidForMillis +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "login-request.v1+json";
}
