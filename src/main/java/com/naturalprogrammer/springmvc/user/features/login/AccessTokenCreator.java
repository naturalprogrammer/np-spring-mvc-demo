package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.user.services.UserService;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Date;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.ACCESS_TOKEN_VALID_MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
class AccessTokenCreator {

    private final UserService userService;
    private final ProblemComposer problemComposer;
    private final JwsService jwsService;
    private final Clock clock;

    public Either<Problem, AccessTokenResource> create(UUID userId) {
        if (userService.isSelfOrAdmin(userId)) {
            var accessTokenValidUntil = clock.instant().plusMillis(ACCESS_TOKEN_VALID_MILLIS + 1).truncatedTo(SECONDS);
            var accessToken = jwsService.createToken(userId.toString(), Date.from(accessTokenValidUntil));
            return Either.right(new AccessTokenResource(accessToken, accessTokenValidUntil));
        }
        return Either.left(problemComposer.compose(ProblemType.NOT_FOUND, "User %s not found".formatted(userId)));
    }
}