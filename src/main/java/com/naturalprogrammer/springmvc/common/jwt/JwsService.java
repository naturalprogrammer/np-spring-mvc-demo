package com.naturalprogrammer.springmvc.common.jwt;

import com.naturalprogrammer.springmvc.config.MyProperties;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;

@Slf4j
@Component
public class JwsService extends AbstractJwtService {

    private final String keyId;
    private final JWSSigner signer;
    private final JWSVerifier verifier;

    public JwsService(Clock clock, MyProperties properties) throws JOSEException {
        super(clock, properties);

        keyId = properties.jws().id();
        var key = new RSAKey
                .Builder(properties.jws().publicKey())
                .privateKey(properties.jws().privateKey())
                .keyID(properties.jws().id())
                .build();

        signer = new RSASSASigner(key);
        verifier = new RSASSAVerifier(key);
    }

    @Override
    @SneakyThrows
    public Token createToken(Aud aud, Subject subject, long validForMillis, Map<String, Object> claims) {
        var jws = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(),
                createPayload(aud, subject, validForMillis, claims)
        );
        jws.sign(signer);
        return new Token(jws.serialize());
    }

    @Override
    @SneakyThrows
    protected ParseResult parseToken(Token token) {
        var jws = JWSObject.parse(token.value());
        return jws.verify(verifier)
                ? new ParseResult.Success(JWTClaimsSet.parse(jws.getPayload().toJSONObject()))
                : new ParseResult.VerificationFailed();
    }
}
