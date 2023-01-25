package com.naturalprogrammer.springmvc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "my")
public record MyProperties(
        String homepage,
        Jws jws
) {

    public record Jws(
            String id,
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey
    ) {
    }
}