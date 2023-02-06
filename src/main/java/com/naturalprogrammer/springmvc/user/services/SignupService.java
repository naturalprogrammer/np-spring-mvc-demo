package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.common.mail.MailData;
import com.naturalprogrammer.springmvc.common.mail.MailSender;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    public static final long SIGNUP_TOKEN_VALID_MILLIS = DAYS.toMillis(1);
    public static final long VERIFICATION_TOKEN_VALID_MILLIS = DAYS.toMillis(2);
    
    private final BeanValidator validator;
    private final ProblemComposer problemComposer;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwsService jwsService;
    private final JwsService jweService;
    private final Clock clock;
    private final MessageGetter messageGetter;
    private final MailSender mailSender;

    public Either<Problem, UserResource> signup(SignupRequest request, Locale locale) {

        var trimmedRequest = request.trimmed();
        return validator.validateAndGet(trimmedRequest, ProblemType.INVALID_SIGNUP, () ->
                signupValidated(trimmedRequest, locale));
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

        var user = userRepository.save(createUser(request, locale));
        var token = jwsService.createToken(
                user.getIdStr(),
                SIGNUP_TOKEN_VALID_MILLIS
        );
        UserResource resource = userService.toResponse(user, token);
        log.info("Signed up {}. Returning {}", user, resource);
        sendVerificationMail(user);
        return Either.right(resource);
    }

    private void sendVerificationMail(User user) {
        var verificationToken = createVerificationToken(user);
        var mail = new MailData(
                user.getEmail(),
                messageGetter.getMessage("verification-mail-subject"),
                messageGetter.getMessage("verification-mail-body", user.getDisplayName(), verificationToken),
                null
        );
        mailSender.send(mail);
    }

    private String createVerificationToken(User user) {
        var userIdStr = user.getIdStr();
        return jweService.createToken(
                userIdStr,
                VERIFICATION_TOKEN_VALID_MILLIS,
                Map.of("email", user.getEmail())
        );
    }

    private User createUser(SignupRequest request, Locale locale) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setLocale(locale);
        user.setRoles(Set.of(Role.UNVERIFIED));
        user.setTokensValidFrom(clock.instant().truncatedTo(ChronoUnit.SECONDS));
        return user;
    }
}
