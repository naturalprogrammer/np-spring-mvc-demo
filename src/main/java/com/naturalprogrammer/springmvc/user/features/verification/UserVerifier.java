package com.naturalprogrammer.springmvc.user.features.verification;

import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import com.naturalprogrammer.springmvc.user.services.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVerifier {

    private final BeanValidator validator;
    private final ValidatedUserVerifier validatedUserVerifier;

    public Either<Problem, UserResource> verify(UUID userId, UserVerificationRequest request) {
        log.info("Verifying user {}: {}", userId, request);
        return validator.validateAndGet(request, () -> validatedUserVerifier.verify(userId, request));
    }
}

@Slf4j
@Service
@RequiredArgsConstructor
class ValidatedUserVerifier {

    private final ProblemComposer problemComposer;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JweService jweService;

    public Either<Problem, UserResource> verify(UUID userId, UserVerificationRequest request) {

        if (!userService.isSelfOrAdmin(userId))
            return notFound(userId, request);

        return userRepository.findById(userId)
                .map(user -> verify(user, request))
                .orElseGet(() -> notFound(userId, request));
    }

    private Either<Problem, UserResource> verify(User user, UserVerificationRequest request) {

        return jweService
                .parseToken(request.emailVerificationToken())
                .mapLeft(problemType -> problemComposer.compose(
                        problemType,
                        request.emailVerificationToken(),
                        ErrorCode.TOKEN_VERIFICATION_FAILED,
                        "emailVerificationToken"))
                .flatMap(claims -> verify(user, claims));
    }

    private Either<Problem, UserResource> verify(User user, JWTClaimsSet claims) {

        if (notEqual(claims.getSubject(), user.getIdStr()) ||
                notEqual(claims.getClaim("email"), user.getEmail()))
            return Either.left(problemComposer.compose(ProblemType.TOKEN_VERIFICATION_FAILED, claims.toString()));

        user.getRoles().remove(Role.UNVERIFIED);
        user.getRoles().add(Role.VERIFIED);
        userRepository.save(user);
        return Either.right(userService.toResponse(user));
    }

    private Either<Problem, UserResource> notFound(UUID userId, UserVerificationRequest request) {
        log.warn("User {} not found when trying to verify email with {}", userId, request);
        var problem = problemComposer.composeMessage(ProblemType.NOT_FOUND, "user-not-found", userId);
        return Either.left(problem);
    }

}
