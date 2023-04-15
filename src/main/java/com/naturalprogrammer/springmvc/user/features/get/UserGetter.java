package com.naturalprogrammer.springmvc.user.features.get;

import com.naturalprogrammer.springmvc.common.error.Problem;
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

    private final UserRepository userRepository;
    private final UserService userService;

    public Either<Problem, UserResource> get(UUID userId) {

        if (!userService.isSelfOrAdmin(userId)) {
            log.warn("User {} is not self or admin when trying get user", userId);
            return Either.left(userService.userNotFound(userId));
        }

        return userRepository.findById(userId)
                .map(this::getUserResponse)
                .orElseGet(() -> {
                    log.warn("User {} not found when trying get user", userId);
                    return Either.left(userService.userNotFound(userId));
                });
    }

    private Either<Problem, UserResource> getUserResponse(User user) {
        var response = userService.toResource(user);
        log.info("Got {}", response);
        return Either.right(response);
    }
}
