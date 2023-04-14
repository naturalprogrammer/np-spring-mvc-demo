package com.naturalprogrammer.springmvc.user.features.change_password;

import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static com.naturalprogrammer.springmvc.common.Path.USER;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChangePasswordIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Date future = futureTime();

    @Test
    void should_changePassword() throws Exception {

        // given
        var user = randomUser();
        var oldPassword = "OldPassword9!";
        user.setPassword(passwordEncoder.encode(oldPassword));
        user = userRepository.save(user);
        var accessToken = authTokenCreator.createAccessToken(user.getIdStr(), future.toInstant());
        var newPassword = "newPassword9!";

        // when, then
        mvc.perform(patch(USER + "/password")
                        .contentType(ChangePasswordRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "oldPassword" : "%s",
                                        "newPassword" : "%s"
                                   }
                                """.formatted(oldPassword, newPassword)))
                .andExpect(status().isNoContent());

        user = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
    }
}