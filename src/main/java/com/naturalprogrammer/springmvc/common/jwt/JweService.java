package com.naturalprogrammer.springmvc.common.jwt;

import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;

@Slf4j
@Component
public class JweService extends AbstractJwtService {

    private final JWEHeader header;
    private final DirectEncrypter encrypter;
    private final DirectDecrypter decrypter;

    @SneakyThrows
    public JweService(Clock clock, MyProperties properties) {
        super(clock, properties);

        header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256)
                .keyID(properties.jwe().id())
                .build();

        var keyBytes = properties.jwe().key().getBytes();
        encrypter = new DirectEncrypter(keyBytes);
        decrypter = new DirectDecrypter(keyBytes);
    }

    @Override
    @SneakyThrows
    public String createToken(String subject, long validForMillis, Map<String, Object> claims) {
        var jwe = new JWEObject(header, createPayload(subject, validForMillis, claims));
        jwe.encrypt(encrypter);
        return jwe.serialize();
    }

    @Override
    @SneakyThrows
    protected Either<ProblemType, JWTClaimsSet> getClaims(String token) {
        var jwe = JWEObject.parse(token);
        jwe.decrypt(decrypter);
        return Either.right(JWTClaimsSet.parse(jwe.getPayload().toJSONObject()));
    }
}
