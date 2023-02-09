package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProblemComposer problemComposer;
    private final JwsService jwsService;
    private final Clock clock;

    public static final long DEFAULT_RESOURCE_TOKEN_VALID_MILLIS = DAYS.toMillis(30);
    public static final long ACCESS_TOKEN_VALID_MILLIS = MINUTES.toMillis(30);

    public Either<Problem, AuthTokenResource> create(AuthTokenRequest authTokenRequest) {

        log.info("Creating AuthToken for {}", authTokenRequest);
        return userRepository
                .findByEmail(authTokenRequest.email())
                .map(user -> createAuthToken(user, authTokenRequest))
                .orElseGet(() -> Either.left(problemComposer.compose(ProblemType.WRONG_CREDENTIALS, authTokenRequest.toString())));
    }

    private Either<Problem, AuthTokenResource> createAuthToken(User user, AuthTokenRequest authTokenRequest) {
        return passwordEncoder.matches(authTokenRequest.password(), user.getPassword())
                ? Either.right(create(user.getId(), authTokenRequest.resourceTokenValidForMillis()))
                : Either.left(problemComposer.compose(ProblemType.WRONG_CREDENTIALS, authTokenRequest.toString()));
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
                Map.of("scope", AuthScope.RESOURCE_TOKEN.getValue())
        );
        var accessToken = jwsService.createToken(userIdStr, Date.from(accessTokenValidUntil));

        var authToken = new AuthTokenResource(
                resourceToken,
                accessToken,
                resourceTokenValidUntil,
                accessTokenValidUntil
        );
        log.info("Created {} with resource token valid until {} for user {}", authToken, resourceTokenValidUntil, userId);
        return authToken;
    }
}
