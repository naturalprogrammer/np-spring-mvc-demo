package com.naturalprogrammer.springmvc.user.features.reset_password;

import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Map;

import static com.naturalprogrammer.springmvc.common.Path.RESET_PASSWORD;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.FORGOT_PASSWORD;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResetPasswordIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JweService jweService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Date future = futureTime();


    @Test
    void should_resetPassword() throws Exception {

        // given
        var user = userRepository.save(randomUser());
        var token = jweService.createToken(
                user.getIdStr(),
                future,
                Map.of(PURPOSE, FORGOT_PASSWORD, EMAIL, user.getEmail())
        );
        var newPassword = "SomeNewPassword9!";

        // when, then
        mvc.perform(post(RESET_PASSWORD)
                        .contentType(ResetPasswordRequest.CONTENT_TYPE)
                        .content("""
                                   {
                                        "token" : "%s",
                                        "newPassword" : "%s"
                                  }
                                """.formatted(token, newPassword)))
                .andExpect(status().isNoContent());

        user = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
    }

}