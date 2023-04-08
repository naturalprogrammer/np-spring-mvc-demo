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
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.config.sociallogin.HttpCookieOAuth2AuthorizationRequestRepository.CLIENT_ID_COOKIE_PARAM_NAME;
import static com.naturalprogrammer.springmvc.config.sociallogin.HttpCookieOAuth2AuthorizationRequestRepository.newCookie;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.ACCESS_TOKEN_VALID_MILLIS;
import static com.naturalprogrammer.springmvc.user.features.signup.SignupIntegrationTest.assertClaims;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExchangeResourceTokenIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwsService jwsService;

    @Test
    void should_exchangeResourceToken() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);

        var clientId = UUID.randomUUID().toString();
        var resourceTokenValidForMillis = DAYS.toMillis(3);
        var beginTime = Instant.now().truncatedTo(SECONDS);
        var userIdStr = user.getIdStr();
        var resourceToken = authTokenCreator.createClientSpecificResourceToken(userIdStr);

        // when, then
        var response = mvc.perform(post(USERS + "/{id}/exchange-resource-token", user.getId())
                        .header(AUTHORIZATION, "Bearer " + resourceToken)
                        .header(CONTENT_TYPE, ResourceTokenExchangeRequest.CONTENT_TYPE)
                        .cookie(newCookie(CLIENT_ID_COOKIE_PARAM_NAME, clientId))
                        .content("""
                                {
                                    "%s" : "%s",
                                    "resourceTokenValidForMillis" : %d
                                }
                                """.formatted(CLIENT_ID_COOKIE_PARAM_NAME, clientId, resourceTokenValidForMillis)))
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

        var cookie = response.getCookie(CLIENT_ID_COOKIE_PARAM_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo("");
    }

    @Test
    void should_notExchangeResourceToken_WithNormalResourceToken() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);

        var clientId = UUID.randomUUID().toString();
        var userIdStr = user.getIdStr();
        var resourceToken = authTokenCreator.createResourceToken(userIdStr, futureTime().toInstant());

        // when, then
        mvc.perform(post(USERS + "/{id}/exchange-resource-token", userIdStr)
                        .header(AUTHORIZATION, "Bearer " + resourceToken)
                        .header(CONTENT_TYPE, ResourceTokenExchangeRequest.CONTENT_TYPE)
                        .cookie(newCookie(CLIENT_ID_COOKIE_PARAM_NAME, clientId))
                        .content("""
                                {
                                    "%s" : "%s",
                                    "resourceTokenValidForMillis" : 234356
                                }
                                """.formatted(CLIENT_ID_COOKIE_PARAM_NAME, clientId)))
                .andExpect(status().isForbidden())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer error=\"insufficient_scope\"")));
    }

    @Test
    void shouldNot_exchangeResourceToken_forAnotherUser() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);

        var clientId = UUID.randomUUID().toString();
        var userIdStr = user.getIdStr();
        var resourceToken = authTokenCreator.createClientSpecificResourceToken(userIdStr);

        // when, then
        mvc.perform(post(USERS + "/{id}/exchange-resource-token", UUID.randomUUID())
                        .header(AUTHORIZATION, "Bearer " + resourceToken)
                        .header(CONTENT_TYPE, ResourceTokenExchangeRequest.CONTENT_TYPE)
                        .cookie(newCookie(CLIENT_ID_COOKIE_PARAM_NAME, clientId))
                        .content("""
                                {
                                    "%s" : "%s",
                                    "resourceTokenValidForMillis" : 234356
                                }
                                """.formatted(CLIENT_ID_COOKIE_PARAM_NAME, clientId)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void shouldNot_exchangeResourceToken_forAnotherClient() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);

        var userIdStr = user.getIdStr();
        var resourceToken = authTokenCreator.createClientSpecificResourceToken(userIdStr);

        // when, then
        mvc.perform(post(USERS + "/{id}/exchange-resource-token", UUID.randomUUID())
                        .header(AUTHORIZATION, "Bearer " + resourceToken)
                        .header(CONTENT_TYPE, ResourceTokenExchangeRequest.CONTENT_TYPE)
                        .cookie(newCookie(CLIENT_ID_COOKIE_PARAM_NAME, UUID.randomUUID().toString()))
                        .content("""
                                {
                                    "%s" : "%s",
                                    "resourceTokenValidForMillis" : 234356
                                }
                                """.formatted(CLIENT_ID_COOKIE_PARAM_NAME, UUID.randomUUID().toString())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void shouldNot_exchangeResourceToken_when_noClientCookie() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);

        var userIdStr = user.getIdStr();
        var resourceToken = authTokenCreator.createClientSpecificResourceToken(userIdStr);

        // when, then
        mvc.perform(post(USERS + "/{id}/exchange-resource-token", UUID.randomUUID())
                        .header(AUTHORIZATION, "Bearer " + resourceToken)
                        .header(CONTENT_TYPE, ResourceTokenExchangeRequest.CONTENT_TYPE)
                        .content("""
                                {
                                    "%s" : "%s",
                                    "resourceTokenValidForMillis" : 234356
                                }
                                """.formatted(CLIENT_ID_COOKIE_PARAM_NAME, UUID.randomUUID().toString())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void admin_should_exchangeResourceTokenForAnotherUser() throws Exception {

        // given
        var admin = randomUser();
        admin.setRoles(Set.of(Role.VERIFIED, Role.ADMIN));
        admin = userRepository.save(admin);
        var clientId = UUID.randomUUID().toString();
        var resourceToken = authTokenCreator.createClientSpecificResourceToken(admin.getIdStr());

        // when, then
        mvc.perform(post(USERS + "/{id}/exchange-resource-token", UUID.randomUUID())
                        .header(AUTHORIZATION, "Bearer " + resourceToken)
                        .header(CONTENT_TYPE, ResourceTokenExchangeRequest.CONTENT_TYPE)
                        .cookie(newCookie(CLIENT_ID_COOKIE_PARAM_NAME, clientId))
                        .content("""
                                {
                                    "%s" : "%s",
                                    "resourceTokenValidForMillis" : 234356
                                }
                                """.formatted(CLIENT_ID_COOKIE_PARAM_NAME, clientId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(AuthTokensResource.CONTENT_TYPE))
                .andExpect(jsonPath("resourceToken").isString())
                .andExpect(jsonPath("accessToken").isString())
                .andExpect(jsonPath("resourceTokenValidUntil").isString())
                .andExpect(jsonPath("accessTokenValidUntil").isString());
    }
}