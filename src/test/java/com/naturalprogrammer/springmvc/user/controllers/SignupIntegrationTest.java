package com.naturalprogrammer.springmvc.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SignupIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void should_Signup() throws Exception {

        // given
        var email = "user12styz@example.com";
        var password = "password";
        var displayName = "Sanjay567 Patel336";

        // when, then
        var response = mvc.perform(post(USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                   {
                                        "email" : "%s",
                                        "password" : "%s",
                                        "displayName" : "%s"
                                   }      
                                """.formatted(email, password, displayName)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andReturn()
                .getResponse();

        var userResource = mapper.readValue(response.getContentAsString(), UserResource.class);

        MyUser user = userRepository.findById(UUID.fromString(userResource.id())).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getLocale()).isEqualTo(Locale.ENGLISH);
        assertThat(user.getRoles()).contains(Role.UNVERIFIED);
        assertThat(user.getNewEmail()).isNull();
        assertThat(user.getTokensValidFrom()).isBeforeOrEqualTo(Instant.now());

        assertThat(response.getHeader(LOCATION)).isEqualTo(USERS + "/" + userResource.id());
    }
}