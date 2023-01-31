package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    private final BeanValidator validator;
    private final ProblemComposer problemComposer;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwsService jwsService;
    private final Clock clock;

    public Either<Problem, UserResource> signup(SignupRequest request, Locale locale, String clientId) {

        var trimmedRequest = request.trimmed();
        return validator.validateAndGet(trimmedRequest, ProblemType.INVALID_SIGNUP, () ->
                signupValidated(trimmedRequest, locale, clientId));
    }

    private Either<Problem, UserResource> signupValidated(SignupRequest request, Locale locale, String clientId) {

        if (userRepository.existsByEmail(request.email())) {
            var problem = problemComposer.compose(
                    ProblemType.USED_EMAIL,
                    request.toString(),
                    ErrorCode.USED_EMAIL,
                    "email");
            return Either.left(problem);
        }

        var user = userRepository.save(createUser(request, locale));
        var token = jwsService.createToken(
                clientId,
                user.getId().toString(),
                DAYS.toMillis(1)
        );
        UserResource resource = userService.toResponse(user, token);
        log.info("Signed up {}. Returning {}", user, resource);
        return Either.right(resource);
    }

    private User createUser(SignupRequest request, Locale locale) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setLocale(locale);
        user.setRoles(Set.of(Role.EMAIL_UNVERIFIED, Role.USER));
        user.setTokensValidFrom(clock.instant().truncatedTo(ChronoUnit.SECONDS));
        return user;
    }
}
