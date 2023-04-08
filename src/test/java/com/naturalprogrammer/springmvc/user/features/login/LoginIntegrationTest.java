package com.naturalprogrammer.springmvc.user.features.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Set;

import static com.naturalprogrammer.springmvc.common.Path.LOGIN;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.WRONG_CREDENTIALS;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.ACCESS_TOKEN_VALID_MILLIS;
import static com.naturalprogrammer.springmvc.user.features.signup.SignupIntegrationTest.assertClaims;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwsService jwsService;

    @Test
    void should_login() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var resourceTokenValidForMillis = DAYS.toMillis(7);
        var beginTime = Instant.now().truncatedTo(SECONDS);

        // when, then
        var response = mvc.perform(post(LOGIN)
                        .contentType(LoginRequest.CONTENT_TYPE)
                        .content("""
                                {
                                    "email" : "%s",
                                    "password" : "Password9!",
                                    "resourceTokenValidForMillis" : %d
                                }
                                """.formatted(user.getEmail(), resourceTokenValidForMillis)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(AuthTokensResource.CONTENT_TYPE))
                .andExpect(jsonPath("resourceToken").isString())
                .andExpect(jsonPath("accessToken").isString())
                .andExpect(jsonPath("resourceTokenValidUntil").isString())
                .andExpect(jsonPath("accessTokenValidUntil").isString())
                .andReturn()
                .getResponse();

        var endTime = Instant.now().truncatedTo(SECONDS);
        var authTokens = mapper.readValue(response.getContentAsString(), AuthTokensResource.class);
        assertThat(authTokens.resourceToken()).isNotNull();
        assertThat(authTokens.accessToken()).isNotNull();
        assertThat(authTokens.resourceTokenValidUntil()).isAfterOrEqualTo(
                beginTime.plusMillis(resourceTokenValidForMillis));
        assertThat(authTokens.resourceTokenValidUntil()).isBeforeOrEqualTo(
                endTime.plusMillis(resourceTokenValidForMillis));
        assertThat(authTokens.accessTokenValidUntil()).isAfterOrEqualTo(
                beginTime.plusMillis(ACCESS_TOKEN_VALID_MILLIS));
        assertThat(authTokens.accessTokenValidUntil()).isBeforeOrEqualTo(
                endTime.plusMillis(ACCESS_TOKEN_VALID_MILLIS));

        assertClaims(
                beginTime,
                endTime,
                user.getId(),
                jwsService.parseToken(authTokens.accessToken()),
                ACCESS_TOKEN_VALID_MILLIS,
                "normal"
        );
        assertClaims(
                beginTime,
                endTime,
                user.getId(),
                jwsService.parseToken(authTokens.resourceToken()),
                resourceTokenValidForMillis,
                AuthScope.AUTH_TOKENS.getValue()
        );
    }

    @Test
    void should_notLogin_when_wrongPasswordGiven() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);

        // when, then
        mvc.perform(post(LOGIN)
                        .contentType(LoginRequest.CONTENT_TYPE)
                        .content("""
                                {
                                    "email" : "%s",
                                    "password" : "WrongPassword9!",
                                    "resourceTokenValidForMillis" : 2343456
                                }
                                """.formatted(user.getEmail())))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type").value(WRONG_CREDENTIALS.getType()));
    }

    @Test
    void should_notLogin_when_emailNotFound() throws Exception {

        mvc.perform(post(LOGIN)
                        .contentType(LoginRequest.CONTENT_TYPE)
                        .content("""
                                {
                                    "email" : "imaginary.user@example.com",
                                    "password" : "Password9!",
                                    "resourceTokenValidForMillis" : 345267
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("type").value(WRONG_CREDENTIALS.getType()))
                .andExpect(jsonPath("title").value("Either the email or password is wrong"))
                .andExpect(jsonPath("status").value("401"))
                .andExpect(jsonPath("errors", hasSize(0)));
    }

    @Test
    void should_notLogin_when_emailIsBlank() throws Exception {

        // when, then
        mvc.perform(post(LOGIN)
                        .contentType(LoginRequest.CONTENT_TYPE)
                        .content("""
                                {
                                    "email" : "",
                                    "password" : "Password9!",
                                    "resourceTokenValidForMillis" : 345267
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type").value(INVALID_DATA.getType()));
    }

}