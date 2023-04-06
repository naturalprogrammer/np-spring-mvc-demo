package com.naturalprogrammer.springmvc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.springframework.util.Base64Utils.encodeToString;

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
            return encodeToString(publicKey().getEncoded());
        }
    }

    public record Jwe(
            String id,
            String key
    ) {
    }
}
