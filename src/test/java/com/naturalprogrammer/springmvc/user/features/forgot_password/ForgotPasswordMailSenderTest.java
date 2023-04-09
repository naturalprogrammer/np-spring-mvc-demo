package com.naturalprogrammer.springmvc.user.features.forgot_password;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.mail.MailData;
import com.naturalprogrammer.springmvc.common.mail.MailSender;
import com.naturalprogrammer.springmvc.config.MyProperties;
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

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.FORGOT_PASSWORD;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.FAKER;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.forgot_password.ForgotPasswordMailSender.FORGOT_PASSWORD_TOKEN_VALID_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordMailSenderTest {

    @Mock
    private MailSender mailSender;

    @Mock
    private MessageGetter messageGetter;

    @Mock
    private JweService jweService;

    @Mock
    private Clock clock;

    @Mock
    private MyProperties properties;

    @Captor
    private ArgumentCaptor<MailData> mailDataCaptor;

    @InjectMocks
    private ForgotPasswordMailSender subject;

    @Test
    void should_sendForgotPasswordMail() {

        // given
        var user = randomUser();
        var now = Instant.now();
        var token = UUID.randomUUID().toString();
        var forgotPasswordSubject = "Forgot Password Subject";
        var forgotPasswordBody = "Forgot Password Body";
        var homepage = FAKER.internet().url();

        given(clock.instant()).willReturn(now);
        given(jweService.createToken(
                user.getIdStr(),
                Date.from(now.plus(FORGOT_PASSWORD_TOKEN_VALID_DAYS, ChronoUnit.DAYS)),
                Map.of(
                        PURPOSE, FORGOT_PASSWORD,
                        EMAIL, user.getEmail()
                )
        )).willReturn(token);
        given(properties.homepage()).willReturn(homepage);
        given(messageGetter.getMessage("forgot-password-mail-subject")).willReturn(forgotPasswordSubject);
        given(messageGetter.getMessage("forgot-password-mail-body",
                user.getDisplayName(), homepage, token)).willReturn(forgotPasswordBody);
        willDoNothing().given(mailSender).send(any());

        // when
        subject.send(user);

        // then
        verify(mailSender).send(mailDataCaptor.capture());
        var mailData = mailDataCaptor.getValue();
        assertThat(mailData.to()).isEqualTo(user.getEmail());
        assertThat(mailData.subject()).isEqualTo(forgotPasswordSubject);
        assertThat(mailData.bodyHtml()).isEqualTo(forgotPasswordBody);
        assertThat(mailData.attachment()).isNull();
    }
}