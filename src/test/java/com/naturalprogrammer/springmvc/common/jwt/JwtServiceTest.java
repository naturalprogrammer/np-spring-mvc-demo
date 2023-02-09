package com.naturalprogrammer.springmvc.common.jwt;

import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.pastTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class JwtServiceTest {

    private final Clock clock = mock(Clock.class);
    private final MyProperties properties = mockProperties();

    @SneakyThrows
    private MyProperties mockProperties() {

        var jwsKey = new RSAKeyGenerator(2048)
                .keyID("foo")
                .generate();

        var properties = mock(MyProperties.class, RETURNS_DEEP_STUBS);
        given(properties.jws().publicKey()).willReturn(jwsKey.toRSAPublicKey());
        given(properties.jws().privateKey()).willReturn(jwsKey.toRSAPrivateKey());
        given(properties.jwe().key()).willReturn("BCBD9D4139418B06DAB351F31B83052D");
        return properties;
    }

    private final JwsService jwsService = new JwsService(clock, properties);
    private final JweService jweService = new JweService(clock, properties);

    private final String homepage = "http://www.test.example.com";

    private final String subject = UUID.randomUUID().toString();
    private final Instant now = Instant.now();
    private final Date future = futureTime();
    private final Date past = pastTime();

    @BeforeEach
    void setUp() {
        given(properties.homepage()).willReturn(homepage);
        given(clock.instant()).willReturn(now);
    }

    @Test
    void should_createAndParseJws() {
        should_createAndParseToken(jwsService);
    }

    @Test
    void should_createAndParseJwe() {
        should_createAndParseToken(jweService);
    }

    private void should_createAndParseToken(AbstractJwtService jwtService) {

        // given
        Map<String, Object> claims = Map.of("claim1", "value1");

        // when
        var token = jwtService.createToken(subject, future, claims);
        var parseResult = jwtService.parseToken(token);

        // then
        assertThat(parseResult.isRight()).isTrue();
        var claimSet = parseResult.getRight().orElseThrow();
        assertThat(claimSet.getIssuer()).isEqualTo(homepage);
        assertThat(claimSet.getIssueTime()).isEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
        assertThat(claimSet.getAudience()).isEqualTo(List.of(homepage));
        assertThat(claimSet.getExpirationTime()).isEqualTo(future);

        assertThat(claimSet.getClaim("claim1")).isEqualTo("value1");
    }

    @Test
    void should_failParsingJws_when_wrongAudience() {
        should_failParsing_when_wrongAudience(jwsService);
    }

    @Test
    void should_failParsingJwe_when_wrongAudience() {
        should_failParsing_when_wrongAudience(jwsService);
    }

    private void should_failParsing_when_wrongAudience(AbstractJwtService jwtService) {

        // given
        var token = jwtService.createToken(subject, future);

        // when
        given(properties.homepage()).willReturn("wrong-audience");
        var parseResult = jwtService.parseToken(token);

        // then
        assertThat(parseResult.getLeft()).hasValue(ProblemType.WRONG_JWT_AUDIENCE);
    }

    @Test
    void should_failParsingJws_when_tokenExpired() {
        should_failParsing_when_tokenExpired(jwsService);
    }

    @Test
    void should_failParsingJwe_when_tokenExpired() {
        should_failParsing_when_tokenExpired(jweService);
    }

    private void should_failParsing_when_tokenExpired(AbstractJwtService jwtService) {

        // given
        var token = jwtService.createToken(subject, past);

        // when
        var parseResult = jwtService.parseToken(token);

        // then
        assertThat(parseResult.getLeft()).hasValue(ProblemType.EXPIRED_JWT);
    }

    @Test
    void should_failParsingJws_when_wrongKey() throws JOSEException {

        // given
        var token = jwsService.createToken(subject, future);

        var anotherKey = new RSAKeyGenerator(2048)
                .keyID("foo")
                .generate();

        given(properties.jws().publicKey()).willReturn(anotherKey.toRSAPublicKey());
        given(properties.jws().privateKey()).willReturn(anotherKey.toRSAPrivateKey());
        var anotherJwsService = new JwsService(clock, properties);

        // when
        var parseResult = anotherJwsService.parseToken(token);

        // then
        assertThat(parseResult.getLeft()).hasValue(ProblemType.TOKEN_VERIFICATION_FAILED);
    }

    @Test
    void should_failParsingJwe_when_wrongKey() {

        // given
        var token = jweService.createToken(subject, future);
        given(properties.jwe().key()).willReturn("D5585149683470B0E2098D28B8D3AD33");
        var anotherJweService = new JweService(clock, properties);

        // when
        var parseResult = anotherJweService.parseToken(token);

        // then
        assertThat(parseResult.getLeft()).hasValue(ProblemType.TOKEN_VERIFICATION_FAILED);
    }

}