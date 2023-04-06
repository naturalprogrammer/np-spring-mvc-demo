package com.naturalprogrammer.springmvc.user.features.display_name_edit;

import com.naturalprogrammer.springmvc.common.error.Problem;
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
import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DisplayNameEditIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    private final Date future = futureTime();

    @Test
    void should_editDisplayName() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.VERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());
        var newName = "Some New DisplayName 293";

        // when, then
        mvc.perform(patch(USERS + "/{id}/display-name", userIdStr)
                        .contentType(UserDisplayNameEditRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "displayName" : "%s"
                                   }    
                                """.formatted(newName)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andExpect(jsonPath("id").value(userIdStr))
                .andExpect(jsonPath("email").value(user.getEmail()))
                .andExpect(jsonPath("displayName").value(newName))
                .andExpect(jsonPath("locale").value("en-IN"))
                .andExpect(jsonPath("roles", hasSize(1)))
                .andExpect(jsonPath("roles", contains(Role.VERIFIED.name())))
                .andExpect(jsonPath("authTokens").doesNotExist());

        // then
        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getDisplayName()).isEqualTo(newName);
    }

    @Test
    void editingDisplayName_should_respondWith422_whenNewDisplayNameIsBlank() throws Exception {

        // given
        var user = randomUser();
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());

        // when, then
        mvc.perform(patch(USERS + "/{id}/display-name", userIdStr)
                        .contentType(UserDisplayNameEditRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "displayName" : ""
                                   }    
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(Problem.CONTENT_TYPE))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("type").value(INVALID_DATA.getType()))
                .andExpect(jsonPath("title").value("Invalid data given. See \"errors\" for details"))
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

        // then
        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getDisplayName()).isEqualTo(user.getDisplayName());
    }
}