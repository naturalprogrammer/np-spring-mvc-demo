package com.naturalprogrammer.springmvc.user.features.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.ACCESS_TOKEN_VALID_MILLIS;
import static com.naturalprogrammer.springmvc.user.features.signup.SignupIntegrationTest.assertClaims;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccessTokenCreationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwsService jwsService;

    private final Date future = futureTime();

    @Test
    void should_createAccessToken() throws Exception {

        // given
        var user = userRepository.save(randomUser());
        var beginTime = Instant.now().truncatedTo(SECONDS);
        var userIdStr = user.getIdStr();
        var resourceToken = authTokenCreator.createResourceToken(userIdStr, future.toInstant());

        // when, then
        var response = mvc.perform(get(USERS + "/{id}/access-token", user.getId())
                        .header(AUTHORIZATION, "Bearer " + resourceToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(AccessTokenResource.CONTENT_TYPE))
                .andExpect(jsonPath("accessToken").isString())
                .andExpect(jsonPath("accessTokenValidUntil").isString())
                .andReturn()
                .getResponse();

        var endTime = Instant.now().truncatedTo(SECONDS);
        var accessToken = mapper.readValue(response.getContentAsString(), AccessTokenResource.class);
        assertThat(accessToken.accessToken()).isNotNull();
        assertThat(accessToken.accessTokenValidUntil()).isAfterOrEqualTo(
                beginTime.plusMillis(ACCESS_TOKEN_VALID_MILLIS));
        assertThat(accessToken.accessTokenValidUntil()).isBeforeOrEqualTo(
                endTime.plusMillis(ACCESS_TOKEN_VALID_MILLIS));

        assertClaims(
                beginTime,
                endTime,
                user.getId(),
                jwsService.parseToken(accessToken.accessToken()),
                ACCESS_TOKEN_VALID_MILLIS,
                "normal"
        );
    }

    @Test
    void should_notCreateAuthTokens_when_authorizedWithAccessToken() throws Exception {

        // given
        var user = userRepository.save(randomUser());
        var accessToken = authTokenCreator.createAccessToken(user.getIdStr(), futureTime().toInstant());

        // when, then
        mvc.perform(get(USERS + "/{id}/access-token", user.getId())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer error=\"insufficient_scope\"")));
    }

    @Test
    void should_notCreateAuthTokens_when_authorizedWithExchangeToken() throws Exception {

        // given
        var user = userRepository.save(randomUser());
        var accessToken = authTokenCreator.createClientSpecificResourceToken(user.getIdStr());

        // when, then
        mvc.perform(get(USERS + "/{id}/access-token", user.getId())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer error=\"insufficient_scope\"")));
    }

}