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

/**
 * Common JWT Service
 */
@RequiredArgsConstructor
public abstract class AbstractJwtService {

	private final Clock clock;
	private final MyProperties properties;

	protected Payload createPayload(Aud aud, Subject subject, Long validForMillis, Map<String, Object> claims) {

		var now = clock.instant();

		var builder = new JWTClaimsSet.Builder()
				.issuer(properties.homepage())
				.subject(subject.value())
				.issueTime(Date.from(now))
				.audience(aud.value())
				.expirationTime(Date.from(now.plusMillis(validForMillis + 1)));
		claims.forEach(builder::claim);

		return new Payload(builder.build().toJSONObject());
	}

	public Token createToken(Aud aud, Subject subject, Long expirationMillis) {
		return createToken(aud, subject, expirationMillis, Collections.emptyMap());
	}

	public abstract Token createToken(Aud aud, Subject subject, long validForMillis, Map<String, Object> claims);

	protected abstract ParseResult parseToken(Token token);

	public ParseResult parseToken(Token token, Aud aud) {
		return parseToken(token).map(claims -> verifyAudience(claims, aud));
	}

	private ParseResult verifyAudience(JWTClaimsSet claims, Aud aud) {
		return claims.getAudience().contains(aud.value())
				? new ParseResult.Success(claims)
				: new ParseResult.WrongAudience();
	}

	public sealed interface ParseResult {
		record Success(JWTClaimsSet claims) implements ParseResult {
		}

		record VerificationFailed() implements ParseResult {
		}

		record WrongAudience() implements ParseResult {
		}

		default ParseResult map(Function<JWTClaimsSet, ParseResult> function) {
			return switch (this) {
				case Success success -> function.apply(success.claims);
				default -> this;
			};
		}
	}
}
