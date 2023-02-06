package com.naturalprogrammer.springmvc.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_SIGNUP;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.USED_EMAIL;
import static com.naturalprogrammer.springmvc.common.mail.LoggingMailSender.sentMails;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SignupIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwsService jwsService;

    @Test
    void should_signup() throws Exception {

        // given
        var email = "user12styz@example.com";
        var password = "Password9!";
        var displayName = "Sanjay567 Patel336";
        sentMails().clear();

        // when, then
        var response = mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
                        .content("""
                                   {
                                        "email" : "%s",
                                        "password" : "%s",
                                        "displayName" : "%s"
                                   }     
                                """.formatted(email, password, displayName)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("email").value(email))
                .andExpect(jsonPath("displayName").value(displayName))
                .andExpect(jsonPath("locale").value("en-IN"))
                .andExpect(jsonPath("roles", hasSize(1)))
                .andExpect(jsonPath("roles", contains(Role.UNVERIFIED.name())))
                .andExpect(jsonPath("token").isString())
                .andReturn()
                .getResponse();

        var userResource = mapper.readValue(response.getContentAsString(), UserResource.class);

        User user = userRepository.findById(UUID.fromString(userResource.id())).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getLocale().toLanguageTag()).isEqualTo("en-IN");
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles()).contains(Role.UNVERIFIED);
        assertThat(user.getNewEmail()).isNull();
        assertThat(user.getTokensValidFrom()).isBeforeOrEqualTo(Instant.now());

        assertThat(response.getHeader(LOCATION)).isEqualTo(USERS + "/" + userResource.id());
        var parseResult = jwsService.parseToken(userResource.token());
        assertThat(parseResult.isRight()).isTrue();
        JWTClaimsSet claims = parseResult.getRight().orElseThrow();
        assertThat(claims.getIssuer()).isEqualTo("https://www.my-super-site.example.com");
        assertThat(claims.getIssueTime()).isBeforeOrEqualTo(Instant.now());
        assertThat(claims.getSubject()).isEqualTo(userResource.id());
        assertThat(claims.getAudience()).isEqualTo(List.of("https://www.my-super-site.example.com"));
        assertThat(claims.getExpirationTime()).isAfter(Instant.now());

        assertThat(sentMails()).hasSize(1);
        var mailData = sentMails().get(0);
        assertThat(mailData.to()).isEqualTo(email);
        assertThat(mailData.bodyHtml()).contains(displayName);
        assertThat(mailData.subject()).isEqualTo("Please verify your email");
    }

    @Test
    void should_preventSignup_when_displayNameIsBlank() throws Exception {

        // when, then
        mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
                        .locale(Locale.forLanguageTag("or"))
                        .content("""
                                   {
                                        "email" : "user23Alpha@example.com",
                                        "password" : "Password9!",
                                        "displayName" : "  "
                                   }     
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(Problem.CONTENT_TYPE))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("type").value(INVALID_SIGNUP.getType()))
                .andExpect(jsonPath("title").value("Invalid data when signing up"))
                .andExpect(jsonPath("status").value("422"))
                .andExpect(jsonPath("errors", hasSize(2)))
                .andExpect(jsonPath("errors[?(" +
                        "@.code == 'Size' &&" +
                        "@.message == 'size must be between 1 and 50' &&" +
                        "@.field == 'displayName'" +
                        ")]").exists())
                .andExpect(jsonPath("errors[?(" +
                        "@.code == 'NotBlank' &&" +
                        "@.message == 'ଖାଲି ହେବା ଉଚିତ୍ ନୁହେଁ' &&" +
                        "@.field == 'displayName'" +
                        ")]").exists());

        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void should_preventSignup_when_emailIsAlreadyUsed() throws Exception {

        // given
        var user = userRepository.save(randomUser());

        // when, then
        mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
                        .content("""
                                   {
                                        "email" : "%s",
                                        "password" : "Password9!",
                                        "displayName" : "Sanjay457 Patel983"
                                   }     
                                """.formatted(user.getEmail())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(Problem.CONTENT_TYPE))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("type").value(USED_EMAIL.getType()))
                .andExpect(jsonPath("title").value("Email already used"))
                .andExpect(jsonPath("status").value("409"))
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].code").value("UsedEmail"))
                .andExpect(jsonPath("errors[0].message").value("Email already used"))
                .andExpect(jsonPath("errors[0].field").value("email"));
    }

}