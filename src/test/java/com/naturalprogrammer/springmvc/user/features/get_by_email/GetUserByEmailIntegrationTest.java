package com.naturalprogrammer.springmvc.user.features.get_by_email;

import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Set;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GetUserByEmailIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    private final Date future = futureTime();

    @Test
    void admin_should_getUsers() throws Exception {

        // given
        var admin = randomUser();
        admin.setRoles(Set.of(Role.VERIFIED, Role.ADMIN));
        admin = userRepository.save(admin);
        var accessToken = authTokenCreator.createAccessToken(admin.getIdStr(), future.toInstant());

        var user = userRepository.save(randomUser());

        // when, then
        mvc.perform(get(USERS)
                        .param("email", user.getEmail())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UserResource.LIST_TYPE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(user.getIdStr()))
                .andExpect(jsonPath("$[0].email").value(user.getEmail()))
                .andExpect(jsonPath("$[0].displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$[0].locale").value(user.getLocale().toLanguageTag()))
                .andExpect(jsonPath("$[0].roles", hasSize(0)))
                .andExpect(jsonPath("$[0].authTokens").doesNotExist());
    }

    @Test
    void admin_should_getEmptyList_when_userNotFound() throws Exception {

        // given
        var admin = randomUser();
        admin.setRoles(Set.of(Role.VERIFIED, Role.ADMIN));
        admin = userRepository.save(admin);
        var accessToken = authTokenCreator.createAccessToken(admin.getIdStr(), future.toInstant());

        // when, then
        mvc.perform(get(USERS)
                        .param("email", "foo" + admin.getEmail())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UserResource.LIST_TYPE))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void nonAdmin_shouldNot_getUsers() throws Exception {

        // given
        var nonAdmin = randomUser();
        nonAdmin.setRoles(Set.of(Role.VERIFIED));
        nonAdmin = userRepository.save(nonAdmin);
        var accessToken = authTokenCreator.createAccessToken(nonAdmin.getIdStr(), future.toInstant());

        // when, then
        mvc.perform(get(USERS)
                        .param("email", nonAdmin.getEmail())
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer error=\"insufficient_scope\"")));
    }

}