package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import static com.naturalprogrammer.springmvc.common.Path.USER;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.EMAIL_CHANGE;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.FAKER;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.verification.VerificationMailSender.VERIFICATION_TOKEN_VALID_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChangeEmailIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    @Autowired
    private JweService jweService;

    private final Date future = futureTime();

    @Test
    void Should_ChangeEmail() throws Exception {

        // given
        var user = randomUser();
        var newEmail = FAKER.internet().emailAddress();
        user.setNewEmail(newEmail);
        user = userRepository.save(user);
        var accessToken = authTokenCreator.createAccessToken(user.getIdStr(), future.toInstant());

        var now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var verificationToken = jweService.createToken(
                user.getIdStr(),
                Date.from(now.plus(VERIFICATION_TOKEN_VALID_DAYS, ChronoUnit.DAYS)),
                Map.of(
                        PURPOSE, EMAIL_CHANGE,
                        EMAIL, newEmail
                )
        );

        // when, then
        mvc.perform(patch(USER + "/email-change-request")
                        .contentType(UserEmailChangeVerificationRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "emailVerificationToken" : "%s"
                                   }
                                """.formatted(verificationToken)))
                .andExpect(status().isNoContent());

        user = userRepository.findById(user.getId()).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(newEmail);
        assertThat(user.getNewEmail()).isNull();
        assertThat(user.getTokensValidFrom()).isAfterOrEqualTo(now);
    }
}