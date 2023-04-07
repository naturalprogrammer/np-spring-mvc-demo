package com.naturalprogrammer.springmvc.user.features.get;

import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GetUserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    private final Date future = futureTime();

    @Test
    void should_getUser() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());

        // when, then
        mvc.perform(get(USERS + "/{id}", userIdStr)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andExpect(jsonPath("id").value(userIdStr))
                .andExpect(jsonPath("email").value(user.getEmail()))
                .andExpect(jsonPath("displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("locale").value("en-IN"))
                .andExpect(jsonPath("roles", hasSize(1)))
                .andExpect(jsonPath("roles", contains(Role.VERIFIED.name())))
                .andExpect(jsonPath("authTokens").doesNotExist());
    }

    @Test
    void should_getUser_when_accessedByAdmin() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();

        var admin = randomUser();
        admin.setRoles(Set.of(Role.VERIFIED, Role.ADMIN));
        admin = userRepository.save(admin);

        var accessToken = authTokenCreator.createAccessToken(admin.getIdStr(), future.toInstant());

        // when, then
        mvc.perform(get(USERS + "/{id}", userIdStr)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void should_notGetUser_when_accessedByNeitherUserNorAdmin() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();

        var anotherUser = randomUser();
        anotherUser.setRoles(Set.of(Role.VERIFIED));
        anotherUser = userRepository.save(anotherUser);

        var accessToken = authTokenCreator.createAccessToken(anotherUser.getIdStr(), future.toInstant());

        // when, then
        mvc.perform(get(USERS + "/{id}", userIdStr)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("type").value(ProblemType.NOT_FOUND.getType()))
                .andExpect(jsonPath("title").value("Entity not found"))
                .andExpect(jsonPath("status").value(404))
                .andExpect(jsonPath("detail").value("User %s not found".formatted(userIdStr)))
                .andExpect(jsonPath("errors", hasSize(0)));
    }

    @Test
    void should_notGetUser_when_unauthenticated() throws Exception {

        // when, then
        mvc.perform(get(USERS + "/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

}