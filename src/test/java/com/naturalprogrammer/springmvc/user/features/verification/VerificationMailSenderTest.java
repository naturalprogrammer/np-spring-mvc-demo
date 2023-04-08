package com.naturalprogrammer.springmvc.user.features.verification;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.mail.MailData;
import com.naturalprogrammer.springmvc.common.mail.MailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.verification.VerificationMailSender.VERIFICATION_TOKEN_VALID_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerificationMailSenderTest {

    @Mock
    private JweService jweService;

    @Mock
    private Clock clock;

    @Mock
    private MessageGetter messageGetter;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private VerificationMailSender subject;

    @Captor
    private ArgumentCaptor<MailData> mailCaptor;

    @Test
    void should_sendVerificationMail() {

        // given
        var user = randomUser();
        var now = Instant.now();
        var verificationToken = UUID.randomUUID().toString();
        var mailSubject = "Verification mail subject";
        var mailBody = "Verification mail body %s %s".formatted(user.getDisplayName(), verificationToken);
        given(clock.instant()).willReturn(now);
        given(jweService.createToken(
                user.getIdStr(),
                Date.from(now.plus(VERIFICATION_TOKEN_VALID_DAYS, ChronoUnit.DAYS)),
                Map.of("email", user.getEmail())))
                .willReturn(verificationToken);
        given(messageGetter.getMessage("verification-mail-subject")).willReturn(mailSubject);
        given(messageGetter.getMessage("verification-mail-body", user.getDisplayName(), verificationToken))
                .willReturn(mailBody);
        willDoNothing().given(mailSender).send(any());

        // when
        subject.send(user);

        // then
        verify(mailSender).send(mailCaptor.capture());
        var mailData = mailCaptor.getValue();
        assertThat(mailData.to()).isEqualTo(user.getEmail());
        assertThat(mailData.subject()).isEqualTo(mailSubject);
        assertThat(mailData.bodyHtml()).isEqualTo(mailBody);
        assertThat(mailData.attachment()).isNull();
    }
}