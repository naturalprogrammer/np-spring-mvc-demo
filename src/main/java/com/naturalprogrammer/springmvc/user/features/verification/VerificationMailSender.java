package com.naturalprogrammer.springmvc.user.features.verification;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.mail.MailData;
import com.naturalprogrammer.springmvc.common.mail.MailSender;
import com.naturalprogrammer.springmvc.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VerificationMailSender {

    public static final long VERIFICATION_TOKEN_VALID_DAYS = 1;

    private final JweService jweService;
    private final Clock clock;
    private final MessageGetter messageGetter;
    private final MailSender mailSender;

    public void send(User user) {
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
        return jweService.createToken(
                user.getIdStr(),
                Date.from(clock.instant().plus(VERIFICATION_TOKEN_VALID_DAYS, ChronoUnit.DAYS)),
                Map.of("email", user.getEmail())
        );
    }
}
