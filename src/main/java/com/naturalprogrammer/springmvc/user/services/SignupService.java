package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.error.ErrorCode;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    private final LocalValidatorFactoryBean validator;
    private final ProblemComposer problemComposer;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwsService jwsService;
    private final Clock clock;

    public Result signup(SignupRequest request, Locale locale, String clientId) {

        request = new SignupRequest(
                trim(request.email()), trim(request.password()), trim(request.displayName())
        );
        log.info("Received {}", request);
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            var problem = problemComposer.compose(ProblemType.INVALID_SIGNUP, request.toString(), violations);
            return new Result.Error(problem);
        }

        if (userRepository.existsByEmail(request.email())) {
            var problem = problemComposer.compose(
                    ProblemType.USED_EMAIL,
                    request.toString(),
                    ErrorCode.USED_EMAIL,
                    "email");
            return new Result.Error(problem);
        }

        var user = userRepository.save(createUser(request, locale));
        var token = jwsService.createToken(
                clientId,
                user.getId().toString(),
                DAYS.toMillis(1)
        );
        UserResource resource = userService.toResponse(user, token);
        log.info("Returning {} for {}", resource, user);
        return new Result.Success(resource);
    }

    private User createUser(SignupRequest request, Locale locale) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setLocale(locale);
        user.setRoles(List.of(Role.EMAIL_UNVERIFIED, Role.USER));
        user.setTokensValidFrom(clock.instant());
        return user;
    }

    public sealed interface Result {
        record Success(UserResource response) implements Result {
        }

        record Error(Problem problem) implements Result {
        }
    }
}
