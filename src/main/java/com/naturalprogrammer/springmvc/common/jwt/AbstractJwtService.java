package com.naturalprogrammer.springmvc.common.jwt;

import com.naturalprogrammer.springmvc.config.MyProperties;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class AbstractJwtService {

	private final Clock clock;
	private final MyProperties properties;

	protected Payload createPayload(String aud, String subject, Long validForMillis, Map<String, Object> claims) {

		var now = clock.instant();

		var builder = new JWTClaimsSet.Builder()
				.issuer(properties.homepage())
				.subject(subject)
				.issueTime(Date.from(now))
				.audience(aud)
				.expirationTime(Date.from(now.plusMillis(validForMillis + 1)));
		claims.forEach(builder::claim);

		return new Payload(builder.build().toJSONObject());
	}

	public String createToken(String aud, String subject, long validForMillis) {
		return createToken(aud, subject, validForMillis, Collections.emptyMap());
	}

	public abstract String createToken(String aud, String subject, long validForMillis, Map<String, Object> claims);

	protected abstract ParseResult parseToken(String token);

	public ParseResult parseToken(String token, String aud) {
		return parseToken(token)
				.map(this::verifyExpiration)
				.map(claims -> verifyAudience(claims, aud));
	}

	private ParseResult verifyAudience(JWTClaimsSet claims, String aud) {
		return claims.getAudience().contains(aud)
				? new ParseResult.Success(claims)
				: new ParseResult.WrongAudience();
	}

	private ParseResult verifyExpiration(JWTClaimsSet claims) {
		return claims.getExpirationTime().after(Date.from(clock.instant()))
				? new ParseResult.Success(claims)
				: new ParseResult.ExpiredToken();
	}

	public sealed interface ParseResult {
		record Success(JWTClaimsSet claims) implements ParseResult {
		}

		record VerificationFailed() implements ParseResult {
		}

		record WrongAudience() implements ParseResult {
		}

		record ExpiredToken() implements ParseResult {

		}

		default ParseResult map(Function<JWTClaimsSet, ParseResult> function) {
			return switch (this) {
				case Success success -> function.apply(success.claims);
				default -> this;
			};
		}
	}
}
