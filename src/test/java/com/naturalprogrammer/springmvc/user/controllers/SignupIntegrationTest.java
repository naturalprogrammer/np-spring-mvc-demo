package com.naturalprogrammer.springmvc.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.jwt.AbstractJwtService.ParseResult;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.CommonUtils.X_FORWARDED_FOR;
import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_SIGNUP;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.USED_EMAIL;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
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
        var clientIp = "192.55.352.1";

        // when, then
        var response = mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
                        .header(X_FORWARDED_FOR, clientIp)
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
                .andExpect(jsonPath("token").isString())
                .andReturn()
                .getResponse();

        var userResource = mapper.readValue(response.getContentAsString(), UserResource.class);

        MyUser user = userRepository.findById(UUID.fromString(userResource.id())).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getLocale().toLanguageTag()).isEqualTo("en-IN");
        assertThat(user.getRoles()).contains(Role.UNVERIFIED);
        assertThat(user.getNewEmail()).isNull();
        assertThat(user.getTokensValidFrom()).isBeforeOrEqualTo(Instant.now());

        assertThat(response.getHeader(LOCATION)).isEqualTo(USERS + "/" + userResource.id());
        JWTClaimsSet claims = ((ParseResult.Success) jwsService.parseToken(userResource.token(), clientIp)).claims();
        assertThat(claims.getIssuer()).isEqualTo("https://www.my-super-site.example.com");
        assertThat(claims.getIssueTime()).isBeforeOrEqualTo(Instant.now());
        assertThat(claims.getSubject()).isEqualTo(userResource.id());
        assertThat(claims.getAudience()).isEqualTo(List.of(clientIp));
        assertThat(claims.getExpirationTime()).isAfter(Instant.now());
    }

    @Test
    void should_preventSignup_when_displayNameIsBlank() throws Exception {

        // when, then
        mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
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
                        "@.message == 'must not be blank' &&" +
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