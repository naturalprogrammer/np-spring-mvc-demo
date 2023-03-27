package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.user.services.UserService;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenCreator {

    private final UserService userService;
    private final ProblemComposer problemComposer;
    private final JwsService jwsService;
    private final Clock clock;

    public static final long DEFAULT_RESOURCE_TOKEN_VALID_MILLIS = DAYS.toMillis(30);
    public static final long CLIENT_SPECIFIC_RESOURCE_TOKEN_VALID_MILLIS = MINUTES.toMillis(1);
    public static final long ACCESS_TOKEN_VALID_MILLIS = MINUTES.toMillis(30);

    public Either<Problem, ResourceTokenResource> create(
            UUID userId,
            Long resourceTokenValidForMillis
    ) {
        return userService.isSelfOrAdmin(userId)
                ? Either.right(create(userId.toString(), resourceTokenValidForMillis))
                : Either.left(problemComposer.compose(ProblemType.NOT_FOUND, "User %s not found".formatted(userId)));
    }

    public ResourceTokenResource create(
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

        var authToken = new ResourceTokenResource(
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
                Map.of("scope", scope.getValue())
        );
    }

    public String createAccessToken(String userIdStr, Instant validUntil) {
        return createTokenWithScope(userIdStr, validUntil, AuthScope.NORMAL);
    }

    public String createResourceToken(String userIdStr, Instant validUntil) {
        return createTokenWithScope(userIdStr, validUntil, AuthScope.RESOURCE_TOKEN);
    }

    public String createClientSpecificResourceToken(String userIdStr) {
        var validUntil = clock.instant()
                .plusMillis(CLIENT_SPECIFIC_RESOURCE_TOKEN_VALID_MILLIS + 1)
                .truncatedTo(SECONDS);
        return createTokenWithScope(userIdStr, validUntil, AuthScope.EXCHANGE_RESOURCE_TOKEN);
    }
}
