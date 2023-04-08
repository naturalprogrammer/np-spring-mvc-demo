package com.naturalprogrammer.springmvc.user.features.resend_verification;

import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.common.mail.LoggingMailSender.sentMails;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResendVerificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    @Test
    void should_resendVerificationMail() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.UNVERIFIED));
        user = userRepository.save(user);
        var accessToken = authTokenCreator.createAccessToken(user.getIdStr(), futureTime().toInstant());
        sentMails().clear();

        // when
        mvc.perform(post(USERS + "/{id}/verifications", user.getId())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // then
        assertThat(sentMails()).hasSize(1);
        var mailData = sentMails().get(0);
        assertThat(mailData.to()).isEqualTo(user.getEmail());
        assertThat(mailData.bodyHtml()).contains(user.getDisplayName());
        assertThat(mailData.subject()).isEqualTo("Please verify your email");
    }

}