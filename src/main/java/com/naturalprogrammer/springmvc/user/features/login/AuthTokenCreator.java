package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.user.services.UserService;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.AUTH;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.SCOPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenCreator {

    public static final long DEFAULT_RESOURCE_TOKEN_VALID_MILLIS = DAYS.toMillis(30);
    public static final String RESOURCE_TOKEN_VALID_MILLIS_DESCR = "For the milliseconds the returned resource-token is valid. E.g. 1209600000 for 15 days. If not provided, default is 30 days";

    public static final long CLIENT_SPECIFIC_RESOURCE_TOKEN_VALID_MILLIS = MINUTES.toMillis(1);
    public static final long ACCESS_TOKEN_VALID_MILLIS = MINUTES.toMillis(30);

    private final UserService userService;
    private final ObjectFactory<ProblemBuilder> problemBuilder;
    private final JwsService jwsService;
    private final Clock clock;

    public Either<Problem, AuthTokensResource> create(
            UUID userId,
            Long resourceTokenValidForMillis
    ) {
        return userService.isSelfOrAdmin(userId)
                ? Either.right(create(userId.toString(), resourceTokenValidForMillis))
                : Either.left(problemBuilder.getObject().build(ProblemType.NOT_FOUND, "User %s not found".formatted(userId)));
    }

    public AuthTokensResource create(
            String userIdStr,
            Long resourceTokenValidForMillis
    ) {
        if (resourceTokenValidForMillis == null)
            resourceTokenValidForMillis = DEFAULT_RESOURCE_TOKEN_VALID_MILLIS;

        var now = clock.instant();
        var resourceTokenValidUntil = now.plusMillis(resourceTokenValidForMillis + 1).truncatedTo(SECONDS);
        var accessTokenValidUntil = now.plusMillis(ACCESS_TOKEN_VALID_MILLIS + 1).truncatedTo(SECONDS);
        var resourceToken = createResourceToken(userIdStr, resourceTokenValidUntil);
        var accessToken = createAccessToken(userIdStr, accessTokenValidUntil);

        var authToken = new AuthTokensResource(
                resourceToken,
                accessToken,
                resourceTokenValidUntil,
                accessTokenValidUntil
        );
        log.info("Created {} with resource token valid until {} for user {}", authToken, resourceTokenValidUntil, userIdStr);
        return authToken;
    }

    private String createTokenWithScope(String userIdStr, Instant validUntil, AuthScope scope) {
        return jwsService.createToken(
                userIdStr,
                Date.from(validUntil),
                Map.of(PURPOSE, AUTH, SCOPE, scope.getValue())
        );
    }

    public String createAccessToken(String userIdStr, Instant validUntil) {
        return createTokenWithScope(userIdStr, validUntil, AuthScope.NORMAL);
    }

    public String createResourceToken(String userIdStr, Instant validUntil) {
        return createTokenWithScope(userIdStr, validUntil, AuthScope.AUTH_TOKENS);
    }

    public String createClientSpecificResourceToken(String userIdStr) {
        var validUntil = clock.instant()
                .plusMillis(CLIENT_SPECIFIC_RESOURCE_TOKEN_VALID_MILLIS + 1)
                .truncatedTo(SECONDS);
        return createTokenWithScope(userIdStr, validUntil, AuthScope.EXCHANGE_RESOURCE_TOKEN);
    }
}
