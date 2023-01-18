package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.SignupService.SignupResult.ValidationError;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    private final LocalValidatorFactoryBean validator;
    private final ProblemComposer problemComposer;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final Clock clock;

    public SignupResult signup(SignupRequest request, Locale locale) {

        request = new SignupRequest(
                request.email().trim(), request.password().trim(), request.displayName().trim()
        );
        log.info("Received {}", request);
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            var problem = problemComposer.compose(ProblemType.INVALID_SIGNUP, request.toString(), violations);
            return new ValidationError(problem);
        }

        MyUser user = userRepository.save(createUser(request, locale));
        String token = ""; // TODO: Create a JWT token for the user
        UserResource resource = userService.toResponse(user, token);
        log.info("Returning {} for {}", resource, user);
        return new SignupResult.Success(resource);
    }

    private MyUser createUser(SignupRequest request, Locale locale) {

        MyUser user = new MyUser();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setLocale(locale);
        user.setRoles(List.of(Role.UNVERIFIED));
        user.setTokensValidFrom(clock.instant());
        return user;
    }


    public sealed interface SignupResult {
        record Success(UserResource response) implements SignupResult {
        }

        record ValidationError(Problem problem) implements SignupResult {
        }
    }
}