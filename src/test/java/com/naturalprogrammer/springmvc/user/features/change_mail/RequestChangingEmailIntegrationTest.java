package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.error.ErrorCode;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.UserTestUtils;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USER;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.PASSWORD_MISMATCH;
import static com.naturalprogrammer.springmvc.common.mail.LoggingMailSender.sentMails;
import static com.naturalprogrammer.springmvc.helpers.MyResultMatchers.result;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.FAKER;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestChangingEmailIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Date future = futureTime();

    @Test
    void should_RequestChangingEmail() throws Exception {

        // given
        var user = randomUser();
        var password = "OldPassword9!";
        user.setPassword(passwordEncoder.encode(password));
        user = userRepository.save(user);
        var accessToken = authTokenCreator.createAccessToken(user.getIdStr(), future.toInstant());

        var newEmail = UserTestUtils.FAKER.internet().emailAddress();
        sentMails().clear();

        // when, then
        mvc.perform(post(USER + "/email-change-request")
                        .contentType(UserEmailChangeRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "oldEmail" : "%s",
                                        "password" : "%s",
                                        "newEmail" : "%s"
                                   }
                                """.formatted(user.getEmail(), password, newEmail)))
                .andExpect(status().isNoContent());

        user = userRepository.findById(user.getId()).orElseThrow();
        assertThat(user.getNewEmail()).isEqualTo(newEmail);

        assertThat(sentMails()).hasSize(1);
        var mailData = sentMails().get(0);
        assertThat(mailData.to()).isEqualTo(newEmail);
        assertThat(mailData.bodyHtml()).contains(user.getDisplayName());
        assertThat(mailData.subject()).isEqualTo("Please verify your email");
    }

    @Test
    void changingEmail_Should_Respond401_When_UserNotFound() throws Exception {

        // given
        var userIdStr = UUID.randomUUID().toString();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());

        // when, then
        mvc.perform(post(USER + "/email-change-request")
                        .contentType(UserEmailChangeRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "oldEmail" : "%s",
                                        "password" : "%s",
                                        "newEmail" : "%s"
                                   }
                                """.formatted(
                                FAKER.internet().emailAddress(),
                                "Password9!",
                                FAKER.internet().emailAddress())
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, "Bearer"));
    }

    @Test
    void changingEmail_Should_Respond403_When_PasswordDiffers() throws Exception {

        // given
        var user = randomUser();
        user.setNewEmail(FAKER.internet().emailAddress());
        user = userRepository.save(user);
        var accessToken = authTokenCreator.createAccessToken(user.getIdStr(), future.toInstant());

        // when, then
        mvc.perform(post(USER + "/email-change-request")
                        .contentType(UserEmailChangeRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "oldEmail" : "%s",
                                        "password" : "%s",
                                        "newEmail" : "%s"
                                   }
                                """.formatted(
                                        user.getEmail(),
                                        "DifferentPassword9!",
                                        user.getNewEmail()
                                )
                        ))
                .andExpect(result().isProblem(403, PASSWORD_MISMATCH.getType(),
                        ErrorCode.PASSWORD_MISMATCH.getCode(), "oldPassword")
                );
    }

}