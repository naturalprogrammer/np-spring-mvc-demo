package com.naturalprogrammer.springmvc.common.jwt;

import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractJwtService {

	private final Clock clock;
	private final MyProperties properties;

	protected Payload createPayload(String subject, Date validUntil, Map<String, Object> claims) {

		var now = clock.instant();

		var builder = new JWTClaimsSet.Builder()
				.issuer(properties.homepage())
				.subject(subject)
				.issueTime(Date.from(now))
				.audience(properties.homepage())
				.expirationTime(validUntil);
		claims.forEach(builder::claim);

		return new Payload(builder.build().toJSONObject());
	}

	public String createToken(String subject, Date validUntil) {
		return createToken(subject, validUntil, Collections.emptyMap());
	}

	public abstract String createToken(String subject, Date validUntil, Map<String, Object> claims);

	protected abstract Either<ProblemType, JWTClaimsSet> getClaims(String token);

	public Either<ProblemType, JWTClaimsSet> parseToken(String token) {
		return getClaims(token)
				.flatMap(this::verifyExpiration)
				.flatMap(this::verifyAudience);
	}

	private Either<ProblemType, JWTClaimsSet> verifyAudience(JWTClaimsSet claims) {
		return claims.getAudience().contains(properties.homepage())
				? Either.right(claims)
				: Either.left(ProblemType.WRONG_JWT_AUDIENCE);
	}

	private Either<ProblemType, JWTClaimsSet> verifyExpiration(JWTClaimsSet claims) {
		return claims.getExpirationTime().after(Date.from(clock.instant()))
				? Either.right(claims)
				: Either.left(ProblemType.EXPIRED_JWT);
	}
}
