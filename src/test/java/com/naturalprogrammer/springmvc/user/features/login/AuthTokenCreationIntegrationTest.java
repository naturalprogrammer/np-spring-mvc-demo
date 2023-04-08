package com.naturalprogrammer.springmvc.user.features.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.ACCESS_TOKEN_VALID_MILLIS;
import static com.naturalprogrammer.springmvc.user.features.signup.SignupIntegrationTest.assertClaims;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthTokenCreationIntegrationTest extends AbstractIntegrationTest {

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
    void should_createAuthTokens() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var resourceTokenValidForMillis = DAYS.toMillis(3);
        var beginTime = Instant.now().truncatedTo(SECONDS);
        var userIdStr = user.getIdStr();
        var resourceToken = authTokenCreator.createResourceToken(userIdStr, future.toInstant());

        // when, then
        var response = mvc.perform(get(USERS + "/{id}/auth-tokens", user.getId())
                        .header("Authorization", "Bearer " + resourceToken)
                        .param("resourceTokenValidForMillis", Long.toString(resourceTokenValidForMillis)))
                .andExpect(status().isOk())
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
    void admin_should_createAuthTokensForAnotherUser() throws Exception {

        // given
        var admin = randomUser();
        admin.setRoles(Set.of(Role.VERIFIED, Role.ADMIN));
        admin = userRepository.save(admin);
        var resourceToken = authTokenCreator.createResourceToken(admin.getIdStr(), future.toInstant());

        // when, then
        mvc.perform(get(USERS + "/{id}/auth-tokens", UUID.randomUUID())
                        .header("Authorization", "Bearer " + resourceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resourceToken").isString())
                .andExpect(jsonPath("accessToken").isString())
                .andExpect(jsonPath("resourceTokenValidUntil").isString())
                .andExpect(jsonPath("accessTokenValidUntil").isString());
    }

    @Test
    void nonAdmin_should_notCreateAuthTokensForAnotherUser() throws Exception {

        // given
        var anotherUser = randomUser();
        anotherUser.setRoles(Set.of(Role.VERIFIED));
        anotherUser = userRepository.save(anotherUser);
        var resourceToken = authTokenCreator.createResourceToken(anotherUser.getIdStr(), future.toInstant());

        // when, then
        mvc.perform(get(USERS + "/{id}/auth-tokens", UUID.randomUUID())
                        .header("Authorization", "Bearer " + resourceToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("type").value(ProblemType.NOT_FOUND.getType()));
    }

    @Test
    void should_notCreateAuthTokens_when_authorizedWithAccessToken() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());

        // when, then
        mvc.perform(get(USERS + "/{id}/auth-tokens", user.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_notCreateAuthTokens_when_authorizedWithExchangeResourceToken() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createClientSpecificResourceToken(userIdStr);

        // when, then
        mvc.perform(get(USERS + "/{id}/auth-tokens", user.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

}