package com.naturalprogrammer.springmvc.user.features.forgot_password;

import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.naturalprogrammer.springmvc.common.Path.FORGOT_PASSWORD;
import static com.naturalprogrammer.springmvc.common.mail.LoggingMailSender.sentMails;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ForgotPasswordIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_initiateForgotPassword() throws Exception {

        // given
        var user = userRepository.save(randomUser());
        sentMails().clear();

        // when, then
        mvc.perform(post(FORGOT_PASSWORD)
                        .contentType(ForgotPasswordRequest.CONTENT_TYPE)
                        .content("""
                                   {
                                        "email" : "%s"
                                  }
                                """.formatted(user.getEmail())))
                .andExpect(status().isNoContent());

        assertThat(sentMails()).hasSize(1);
        var mailData = sentMails().get(0);
        assertThat(mailData.to()).isEqualTo(user.getEmail());
        assertThat(mailData.bodyHtml()).contains(user.getDisplayName());
        assertThat(mailData.subject()).isEqualTo("Forgot password link");
    }

}