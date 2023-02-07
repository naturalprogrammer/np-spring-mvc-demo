package com.naturalprogrammer.springmvc.user.usecases.verification;

import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.dto.UserVerificationRequest;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
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
    private final UserRepository userRepository;
    private final ExistingUserVerifier existingUserVerifier;
    private final NotFoundHandler notFoundHandler;

    public Either<Problem, UserResource> verify(UUID userId, UserVerificationRequest request) {

        return validator.validateAndGet(request, ProblemType.INVALID_VERIFICATION_TOKEN, () ->
                verifyValidated(userId, request));
    }

    private Either<Problem, UserResource> verifyValidated(UUID userId, UserVerificationRequest request) {

        return userRepository.findById(userId)
                .map(user -> existingUserVerifier.verify(user, request))
                .orElse(notFoundHandler.notFound(userId, request));
    }

}

@Service
@RequiredArgsConstructor
class ExistingUserVerifier {

    private final ProblemComposer problemComposer;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JweService jweService;
    private final NotFoundHandler notFoundHandler;

    public Either<Problem, UserResource> verify(User user, UserVerificationRequest request) {

        if (userService.isSelfOrAdmin(user.getId())) {
            return jweService
                    .parseToken(request.emailVerificationToken())
                    .mapLeft(problemType -> problemComposer.compose(
                            problemType,
                            request.emailVerificationToken(),
                            ErrorCode.TOKEN_VERIFICATION_FAILED,
                            "emailVerificationToken"))
                    .flatMap(claims -> verify(user, claims));
        }
        return notFoundHandler.notFound(user.getId(), request);
    }

    private Either<Problem, UserResource> verify(User user, JWTClaimsSet claims) {

        var userIdStr = user.getIdStr();

        if (notEqual(claims.getSubject(), userIdStr) ||
                notEqual(claims.getClaim("email"), user.getEmail()))
            return Either.left(problemComposer.compose(ProblemType.TOKEN_VERIFICATION_FAILED, claims.toString()));

        user.getRoles().remove(Role.UNVERIFIED);
        user.getRoles().add(Role.VERIFIED);
        userRepository.save(user);
        return Either.right(userService.toResponse(user));
    }

}

@Slf4j
@Service
@RequiredArgsConstructor
class NotFoundHandler {

    private final ProblemComposer problemComposer;

    public Either<Problem, UserResource> notFound(UUID userId, UserVerificationRequest request) {
        log.warn("User {} not found when trying to verify email with {}", userId, request);
        var problem = problemComposer.composeMessage(ProblemType.NOT_FOUND, "user-not-found", userId);
        return Either.left(problem);
    }
}