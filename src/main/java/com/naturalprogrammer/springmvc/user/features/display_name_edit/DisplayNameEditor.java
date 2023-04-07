package com.naturalprogrammer.springmvc.user.features.display_name_edit;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
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
class DisplayNameEditor {

    private final BeanValidator validator;
    private final ValidatedDisplayNameEditor validatedDisplayNameEditor;

    public Either<Problem, UserResource> edit(UUID userId, UserDisplayNameEditRequest request) {

        log.info("Editing display name for user {}: {}", userId, request);
        var trimmedRequest = request.trimmed();
        return validator.validateAndGet(trimmedRequest, () -> validatedDisplayNameEditor.edit(userId, trimmedRequest));
    }

}

@Slf4j
@Service
@RequiredArgsConstructor
class ValidatedDisplayNameEditor {

    private final ProblemComposer problemComposer;
    private final UserRepository userRepository;
    private final UserService userService;

    public Either<Problem, UserResource> edit(UUID userId, UserDisplayNameEditRequest request) {

        return userRepository.findById(userId)
                .map(user -> edit(user, request))
                .orElseGet(() -> notFound(userId, request));
    }

    private Either<Problem, UserResource> edit(User user, UserDisplayNameEditRequest request) {

        if (userService.isSelfOrAdmin(user.getId())) {

            user.setDisplayName(request.displayName());
            userRepository.save(user);
            var resource = userService.toResponse(user);

            log.info("Edited name for {}. Returning {}", user, resource);
            return Either.right(resource);
        }
        return notFound(user.getId(), request);
    }

    private Either<Problem, UserResource> notFound(UUID userId, UserDisplayNameEditRequest request) {
        log.warn("User {} not found when trying to edit displayName to {}", userId, request);
        var problem = problemComposer.composeMessage(ProblemType.NOT_FOUND, "user-not-found", userId);
        return Either.left(problem);
    }
}
