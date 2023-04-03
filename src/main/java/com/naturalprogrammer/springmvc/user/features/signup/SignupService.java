package com.naturalprogrammer.springmvc.user.features.signup;

import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import com.naturalprogrammer.springmvc.user.services.UserService;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
class SignupService {

    private final BeanValidator validator;
    private final ProblemComposer problemComposer;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthTokenCreator authTokenCreator;

    public Either<Problem, UserResource> signup(SignupRequest request, Locale locale) {
        log.info("Signing up {} with locale {}", request, locale);
        var trimmedRequest = request.trimmed();
        return validator.validateAndGet(trimmedRequest, () -> signupValidated(trimmedRequest, locale));
    }

    private Either<Problem, UserResource> signupValidated(SignupRequest request, Locale locale) {

        if (userRepository.existsByEmail(request.email())) {
            var problem = problemComposer.compose(
                    ProblemType.USED_EMAIL,
                    request.toString(),
                    ErrorCode.USED_EMAIL,
                    "email");
            return Either.left(problem);
        }

        var user = userService.createUser(request, locale, Role.UNVERIFIED);
        var token = authTokenCreator.create(
                user.getIdStr(),
                request.resourceTokenValidForMillis()
        );
        UserResource resource = userService.toResponse(user, token);
        log.info("Signed up {}. Returning {}", user, resource);
        return Either.right(resource);
    }
}
