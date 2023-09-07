package com.naturalprogrammer.springmvc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@ConfigurationProperties(prefix = "my")
public record MyProperties(
        String homepage,
        String oauth2AuthenticationSuccessUrl,
        Jws jws,
        Jwe jwe
) {

    public record Jws(
            String id,
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey
    ) {

        public String publicKeyString() {
            return Base64.getEncoder().encodeToString(publicKey().getEncoded());
        }
    }

    public record Jwe(
            String id,
            String key
    ) {
    }
}
