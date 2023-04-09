package com.naturalprogrammer.springmvc.user.features.forgot_password;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.mail.MailData;
import com.naturalprogrammer.springmvc.common.mail.MailSender;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.FORGOT_PASSWORD;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
class ForgotPasswordInitiator {

    private final BeanValidator validator;
    private final UserRepository userRepository;
    private final ForgotPasswordMailSender forgotPasswordMailSender;

    public Optional<Problem> initiate(ForgotPasswordRequest request) {

        log.info("Initiating forgot password for {}", request);
        var trimmedRequest = request.trimmed();
        return validator
                .validate(trimmedRequest, INVALID_DATA)
                .or(() -> {
                    initiateValidated(trimmedRequest);
                    return Optional.empty();
                });
    }

    private void initiateValidated(ForgotPasswordRequest request) {
        userRepository
                .findByEmail(request.email())
                .ifPresentOrElse(forgotPasswordMailSender::send, () ->
                        log.warn("User {} not found while sending forgot password link", request)
                );
    }
}

@Slf4j
@Service
@RequiredArgsConstructor
class ForgotPasswordMailSender {

    public static final long FORGOT_PASSWORD_TOKEN_VALID_DAYS = 1;

    private final MailSender mailSender;
    private final MessageGetter messageGetter;
    private final JweService jweService;
    private final Clock clock;
    private final MyProperties properties;

    public void send(User user) {
        var token = createForgotPasswordToken(user);
        var mail = new MailData(
                user.getEmail(),
                messageGetter.getMessage("forgot-password-mail-subject"),
                messageGetter.getMessage("forgot-password-mail-body",
                        user.getDisplayName(), properties.homepage(), token),
                null
        );
        mailSender.send(mail);
    }

    private String createForgotPasswordToken(User user) {
        return jweService.createToken(
                user.getIdStr(),
                Date.from(clock.instant().plus(FORGOT_PASSWORD_TOKEN_VALID_DAYS, ChronoUnit.DAYS)),
                Map.of(
                        PURPOSE, FORGOT_PASSWORD,
                        EMAIL, user.getEmail()
                )
        );
    }
}