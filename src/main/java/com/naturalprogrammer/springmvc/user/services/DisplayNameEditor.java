package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.dto.UserDisplayNameEditRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayNameEditor {

    private final BeanValidator validator;
    private final ProblemComposer problemComposer;
    private final UserRepository userRepository;
    private final UserService userService;

    public Result edit(UUID userId, UserDisplayNameEditRequest request) {

        var trimmedRequest = request.trimmed();
        return validator.validate(trimmedRequest, ProblemType.INVALID_DISPLAY_NAME)
                .map(problem -> (Result) new Result.Error(problem))
                .orElseGet(() -> editValidated(userId, trimmedRequest));
    }

    private Result editValidated(UUID userId, UserDisplayNameEditRequest request) {

        return userRepository.findById(userId)
                .map(user -> edit(user, request))
                .orElse(notFound(userId, request));
    }

    private Result edit(User user, UserDisplayNameEditRequest request) {

        if (userService.isSelfOrAdmin(user.getId())) {

            user.setDisplayName(request.displayName());
            userRepository.save(user);
            var resource = userService.toResponse(user, null);

            log.info("Edited name for {}. Returning {}", user, resource);
            return new Result.Success(resource);
        }
        return notFound(user.getId(), request);
    }

    private Result notFound(UUID userId, UserDisplayNameEditRequest request) {
        log.warn("User {} not found when trying to edit displayName to {}", userId, request);
        var problem = problemComposer.composeMessage(ProblemType.NOT_FOUND, "user-not-found", userId);
        return new Result.Error(problem);
    }

    public sealed interface Result {
        record Success(UserResource response) implements DisplayNameEditor.Result {
        }

        record Error(Problem problem) implements DisplayNameEditor.Result {
        }
    }
}
