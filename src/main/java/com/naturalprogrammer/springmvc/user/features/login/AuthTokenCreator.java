package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Service
@RequiredArgsConstructor
public class AuthTokenCreator {

    private final UserRepository userRepository;
    private final JwsService jwsService;
    private final Clock clock;

    public static final long DEFAULT_RESOURCE_TOKEN_VALID_MILLIS = DAYS.toMillis(30);
    public static final long ACCESS_TOKEN_VALID_MILLIS = MINUTES.toMillis(30);

    public Either<Problem, AuthTokenResource> login(AuthTokenRequest authTokenRequest) {
        return null;
    }

    public AuthTokenResource create(
            UUID userId,
            Long resourceTokenValidForMillis
    ) {
        var userIdStr = userId.toString();
        if (resourceTokenValidForMillis == null)
            resourceTokenValidForMillis = DEFAULT_RESOURCE_TOKEN_VALID_MILLIS;

        var now = clock.instant();
        var resourceTokenValidUntil = now.plusMillis(resourceTokenValidForMillis + 1).truncatedTo(SECONDS);
        var accessTokenValidUntil = now.plusMillis(ACCESS_TOKEN_VALID_MILLIS + 1).truncatedTo(SECONDS);
        var resourceToken = jwsService.createToken(
                userIdStr,
                Date.from(resourceTokenValidUntil),
                Map.of("scope", Scope.RESOURCE_TOKEN.getValue())
        );
        var accessToken = jwsService.createToken(userIdStr, Date.from(accessTokenValidUntil));

        return new AuthTokenResource(
                resourceToken,
                accessToken,
                resourceTokenValidUntil,
                accessTokenValidUntil
        );
    }
}
