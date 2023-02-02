package com.naturalprogrammer.springmvc.common.jwt;

import com.naturalprogrammer.springmvc.common.jwt.AbstractJwtService.ParseResult;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class JwtServiceTest {

    private final Clock clock = mock(Clock.class);
    private final MyProperties properties = mockProperties();

    private final JwsService jwsService = new JwsService(clock, properties);
    private final JweService jweService = new JweService(clock, properties);

    private final String homePage = "http://test";

    private final String aud = "129.456.255.25";
    private final String subject = UUID.randomUUID().toString();
    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
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
        var validForMillis = 15000;
        Map<String, Object> claims = Map.of("claim1", "value1");

        // when
        var token = jwtService.createToken(aud, subject, validForMillis, claims);
        var parseResult = jwtService.parseToken(token, aud);

        // then
        assertThat(parseResult).isInstanceOf(ParseResult.Success.class);

        var claimSet = ((ParseResult.Success) parseResult).claims();
        assertThat(claimSet.getIssuer()).isEqualTo(homePage);
        assertThat(claimSet.getIssueTime()).isEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
        assertThat(claimSet.getAudience()).isEqualTo(List.of(aud));
        assertThat(claimSet.getExpirationTime()).isEqualTo(
                now.plusMillis(validForMillis + 1).truncatedTo(ChronoUnit.SECONDS));

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
        var token = jwtService.createToken(aud, subject, 15000L);

        // when
        var parseResult = jwtService.parseToken(token, "wrong-audience");

        // then
        assertThat(parseResult).isInstanceOf(ParseResult.WrongAudience.class);
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
        var token = jwtService.createToken(aud, subject, -100000L);

        // when
        var parseResult = jwtService.parseToken(token, aud);

        // then
        assertThat(parseResult).isInstanceOf(ParseResult.ExpiredToken.class);
    }

    @SneakyThrows
    private MyProperties mockProperties() {

        RSAKey key = new RSAKeyGenerator(2048)
                .keyID("foo")
                .generate();

        return new MyProperties(
                homePage,
                new MyProperties.Jws(
                        "test-jws-key",
                        key.toRSAPublicKey(),
                        key.toRSAPrivateKey()
                ),
                new MyProperties.Jwe(
                        "test-jwe-key",
                        "BCBD9D4139418B06DAB351F31B83052D"
                )
        );
    }
}