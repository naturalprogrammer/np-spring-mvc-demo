package com.naturalprogrammer.springmvc.common.jwt;

public enum JwtPurpose {

    AUTH,
    EMAIL_VERIFICATION,
    FORGOT_PASSWORD;

    public static final String PURPOSE = "purpose";

}
