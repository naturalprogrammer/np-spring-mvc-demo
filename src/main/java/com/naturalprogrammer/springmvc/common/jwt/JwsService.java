package com.naturalprogrammer.springmvc.common.jwt;

import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;

@Slf4j
@Component
public class JwsService extends AbstractJwtService {

    private final JWSHeader header;
    private final JWSSigner signer;
    private final JWSVerifier verifier;

    @SneakyThrows
    public JwsService(Clock clock, MyProperties properties) {
        
        super(clock, properties);

        header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(properties.jws().id())
                .build();

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
    public String createToken(String subject, long validForMillis, Map<String, Object> claims) {
        var jws = new JWSObject(header, createPayload(subject, validForMillis, claims));
        jws.sign(signer);
        return jws.serialize();
    }

    @Override
    @SneakyThrows
    protected Either<ProblemType, JWTClaimsSet> getClaims(String token) {
        var jws = JWSObject.parse(token);
        return jws.verify(verifier)
                ? Either.right(JWTClaimsSet.parse(jws.getPayload().toJSONObject()))
                : Either.left(ProblemType.JWT_VERIFICATION_FAILED);
    }
}
