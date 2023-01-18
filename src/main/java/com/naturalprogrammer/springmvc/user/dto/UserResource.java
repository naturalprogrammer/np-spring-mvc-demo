package com.naturalprogrammer.springmvc.user.dto;

public record UserResource(
        String id,
        String email,
        String displayName,
        String locale,
        String token
) {
    public static final String CONTENT_TYPE = "application/vnd.com.naturalprogrammer.user.v1+json";
}
