package com.naturalprogrammer.springmvc.user.controllers;

import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.dto.UserVerificationRequest;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.services.SignupService.SIGNUP_TOKEN_VALID_MILLIS;
import static com.naturalprogrammer.springmvc.user.services.SignupService.VERIFICATION_TOKEN_VALID_MILLIS;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VerifyEmailIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwsService jwsService;

    @Autowired
    private JweService jweService;

    @Test
    void should_verifyEmail() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.UNVERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = jwsService.createToken(userIdStr, SIGNUP_TOKEN_VALID_MILLIS);
        var verificationToken = jweService.createToken(
                userIdStr,
                VERIFICATION_TOKEN_VALID_MILLIS,
                Map.of("email", user.getEmail())
        );

        // when, then
        mvc.perform(post(USERS + "/{id}/verification", userIdStr)
                        .contentType(UserVerificationRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "emailVerificationToken" : "%s"
                                  }     
                                """.formatted(verificationToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andExpect(jsonPath("id").value(userIdStr))
                .andExpect(jsonPath("email").value(user.getEmail()))
                .andExpect(jsonPath("displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("locale").value(user.getLocale().toLanguageTag()))
                .andExpect(jsonPath("roles", hasSize(1)))
                .andExpect(jsonPath("roles", hasItem(Role.VERIFIED.name())));
    }
}
