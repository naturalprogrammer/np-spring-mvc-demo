package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.mail.MailData;
import com.naturalprogrammer.springmvc.common.mail.MailSender;
import com.naturalprogrammer.springmvc.config.MyProperties;
import org.assertj.core.api.SoftAssertions;
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

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.EMAIL_CHANGE;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockMessageGetter;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.FAKER;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.verification.VerificationMailSender.VERIFICATION_TOKEN_VALID_DAYS;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@ExtendWith(MockitoExtension.class)
class NewEmailVerificationMailSenderTest {

    @Mock
    private JweService jweService;

    @Mock
    private Clock clock;

    @Mock
    private MessageGetter messageGetter;

    @Mock
    private MailSender mailSender;

    @Mock
    private MyProperties properties;

    @InjectMocks
    private NewEmailVerificationMailSender subject;

    @Captor
    private ArgumentCaptor<MailData> mailCaptor;

    @Test
    void should_sendNewEmailVerificationMail() {

        // given
        var user = randomUser();
        user.setNewEmail(FAKER.internet().emailAddress());
        var now = Instant.now();
        var verificationToken = UUID.randomUUID().toString();
        var homepage = "https://test8567.example.com";

        given(clock.instant()).willReturn(now);
        given(jweService.createToken(
                user.getIdStr(),
                Date.from(now.plus(VERIFICATION_TOKEN_VALID_DAYS, ChronoUnit.DAYS)),
                Map.of(
                        PURPOSE, EMAIL_CHANGE,
                        EMAIL, user.getNewEmail()
                ))).willReturn(verificationToken);
        mockMessageGetter(messageGetter);
        given(properties.homepage()).willReturn(homepage);

        // when
        subject.send(user);

        // then
        verify(mailSender).send(mailCaptor.capture());
        var mailData = mailCaptor.getValue();
        var softly = new SoftAssertions();
        softly.assertThat(mailData.to()).isEqualTo(user.getNewEmail());
        softly.assertThat(mailData.subject()).isEqualTo("verification-mail-subject");
        softly.assertThat(mailData.bodyHtml()).isEqualTo("verification-mail-body"
                + user.getDisplayName() + homepage + verificationToken);
        softly.assertAll();
    }

}