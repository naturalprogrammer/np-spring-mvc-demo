package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.mail.MailData;
import com.naturalprogrammer.springmvc.common.mail.MailSender;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.EMAIL_CHANGE;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.user.features.verification.VerificationMailSender.VERIFICATION_TOKEN_VALID_DAYS;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
class EmailChangeRequestProcessor {

    private final BeanValidator validator;
    private final ValidatedEmailChangeRequestProcessor validatedEmailChangeRequestProcessor;
    private final CommonUtils commonUtils;

    public Optional<Problem> process(UserEmailChangeRequest request) {

        var userId = commonUtils.getUserId().orElseThrow();
        log.info("Processing email change request user {}: {}", userId, request);
        var trimmedRequest = request.trimmed();
        return validator
                .validate(trimmedRequest)
                .or(() -> validatedEmailChangeRequestProcessor.process(userId, trimmedRequest));
    }

}

@Slf4j
@Service
@RequiredArgsConstructor
class ValidatedEmailChangeRequestProcessor {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ObjectFactory<ProblemBuilder> problemComposer;
    private final PasswordEncoder passwordEncoder;
    private final NewEmailVerificationMailSender newEmailVerificationMailSender;

    public Optional<Problem> process(UUID userId, UserEmailChangeRequest request) {

        return userRepository.findById(userId)
                .map(user -> process(user, request))
                .orElseGet(() -> {
                    log.warn("User {} not found when trying to process {}", userId, request);
                    return Optional.of(userService.userNotFound(userId));
                });
    }

    private Optional<Problem> process(User user, UserEmailChangeRequest request) {

        if (notEqual(user.getEmail(), request.oldEmail())) {
            var problem = problemComposer.getObject()
                    .type(ProblemType.EMAIL_MISMATCH)
                    .detailMessage("email-mismatch-for-user", user.getId())
                    .error("oldPassword", ErrorCode.EMAIL_MISMATCH)
                    .build();

            return Optional.of(problem);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            var problem = problemComposer.getObject()
                    .type(ProblemType.PASSWORD_MISMATCH)
                    .detailMessage("password-mismatch-for-user", user.getId())
                    .error("oldPassword", ErrorCode.PASSWORD_MISMATCH)
                    .build();
            return Optional.of(problem);
        }

        user.setNewEmail(request.newEmail());
        newEmailVerificationMailSender.send(user);
        userRepository.save(user);

        log.info("Processed email change request user {}: {}", user, request);
        return Optional.empty();
    }
}

@Service
@RequiredArgsConstructor
class NewEmailVerificationMailSender {

    private final JweService jweService;
    private final Clock clock;
    private final MessageGetter messageGetter;
    private final MailSender mailSender;
    private final MyProperties properties;

    public void send(User user) {
        var verificationToken = createVerificationToken(user);
        var mail = new MailData(
                user.getEmail(),
                messageGetter.getMessage("verification-mail-subject"),
                messageGetter.getMessage("verification-mail-body",
                        user.getDisplayName(), properties.homepage(), verificationToken),
                null
        );
        mailSender.send(mail);
    }

    private String createVerificationToken(User user) {
        return jweService.createToken(
                user.getIdStr(),
                Date.from(clock.instant().plus(VERIFICATION_TOKEN_VALID_DAYS, ChronoUnit.DAYS)),
                Map.of(
                        PURPOSE, EMAIL_CHANGE,
                        EMAIL, user.getNewEmail()
                )
        );
    }
}
