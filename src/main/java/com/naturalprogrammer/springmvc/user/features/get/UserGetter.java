package com.naturalprogrammer.springmvc.user.features.get;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import com.naturalprogrammer.springmvc.user.services.UserService;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class UserGetter {

    private final ProblemComposer problemComposer;
    private final UserRepository userRepository;
    private final UserService userService;

    public Either<Problem, UserResource> get(UUID userId) {
        return userRepository.findById(userId)
                .map(this::get)
                .orElseGet(() -> notFound(userId));
    }

    private Either<Problem, UserResource> get(User user) {

        return userService.isSelfOrAdmin(user.getId())
                ? getUserResponse(user)
                : notFound(user.getId());
    }

    private Either<Problem, UserResource> getUserResponse(User user) {
        var response = userService.toResponse(user);
        log.info("Got {}", response);
        return Either.right(response);
    }

    private Either<Problem, UserResource> notFound(UUID userId) {
        log.warn("User {} not found when trying get user", userId);
        var problem = problemComposer.composeMessage(ProblemType.NOT_FOUND, "user-not-found", userId);
        return Either.left(problem);
    }
}
