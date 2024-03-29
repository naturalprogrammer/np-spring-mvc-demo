package com.naturalprogrammer.springmvc.user.features.verification;

import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.TOKEN_VERIFICATION_FAILED;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.EMAIL_VERIFICATION;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.helpers.MyResultMatchers.result;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.futureTime;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.pastTime;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserVerificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenCreator authTokenCreator;

    @Autowired
    private JweService jweService;

    private final Date future = futureTime();

    @Test
    void should_verifyEmail() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.UNVERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());
        var verificationToken = jweService.createToken(
                userIdStr,
                future,
                Map.of(PURPOSE, EMAIL_VERIFICATION, EMAIL, user.getEmail())
        );

        // when, then
        mvc.perform(put(USERS + "/{id}/verifications", userIdStr)
                        .contentType(UserVerificationRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "emailVerificationToken" : "%s"
                                  }
                                """.formatted(verificationToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andExpect(jsonPath("id").value(userIdStr))
                .andExpect(jsonPath("email").value(user.getEmail()))
                .andExpect(jsonPath("displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("locale").value(user.getLocale().toLanguageTag()))
                .andExpect(jsonPath("roles", hasSize(1)))
                .andExpect(jsonPath("roles", hasItem(Role.VERIFIED.name())));

        user = userRepository.findById(user.getId()).orElseThrow();
        assertThat(user.getRoles()).isEqualTo(Set.of(Role.VERIFIED));
    }

    @Test
    void emailVerification_should_respondWith401_when_notLoggedIn() throws Exception {

        // when, then
        mvc.perform(put(USERS + "/{id}/verifications", UUID.randomUUID())
                        .contentType(UserVerificationRequest.CONTENT_TYPE)
                        .content("""
                                   {
                                        "emailVerificationToken" : "foo"
                                  }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, "Bearer"));
    }

    @Test
    void emailVerification_should_respondWith401_when_ExpiredToken() throws Exception {

        // given
        var userId = UUID.randomUUID();
        var accessToken = authTokenCreator.createAccessToken(userId.toString(), pastTime().toInstant());

        // when, then
        mvc.perform(put(USERS + "/{id}/verifications", userId)
                        .contentType(UserVerificationRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "emailVerificationToken" : "foo"
                                  }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\"")));
    }

    @Test
    void should_preventVerification_when_verificationTokenIsBlank() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.UNVERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());

        // when, then
        mvc.perform(put(USERS + "/{id}/verifications", userIdStr)
                        .contentType(UserVerificationRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "emailVerificationToken" : ""
                                  }
                                """))
                .andExpect(result().isProblem(422, INVALID_DATA.getType(),
                        "NotBlank", "emailVerificationToken"))
                .andExpect(jsonPath("title").value("Invalid data given. See \"errors\" for details"))
                .andExpect(jsonPath("errors[0].message").value("must not be blank"));

        user = userRepository.findById(user.getId()).orElseThrow();
        assertThat(user.getRoles()).isEqualTo(Set.of(Role.UNVERIFIED));
    }

    @Test
    void should_preventVerification_when_verificationIsEncryptedUsingADifferentKey() throws Exception {

        // given
        var user = randomUser();
        user.setRoles(Set.of(Role.UNVERIFIED));
        user = userRepository.save(user);
        var userIdStr = user.getIdStr();
        var accessToken = authTokenCreator.createAccessToken(userIdStr, future.toInstant());

        var properties = mock(MyProperties.class, RETURNS_DEEP_STUBS);
        given(properties.jwe().key()).willReturn("D5585149683470B0E2098D28B8D3AD33");
        JweService anotherJweService = new JweService(Clock.systemUTC(), properties);

        var verificationToken = anotherJweService.createToken(
                userIdStr,
                future,
                Map.of(EMAIL, user.getEmail())
        );

        // when, then
        mvc.perform(put(USERS + "/{id}/verifications", userIdStr)
                        .contentType(UserVerificationRequest.CONTENT_TYPE)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                                   {
                                        "emailVerificationToken" : "%s"
                                  }
                                """.formatted(verificationToken)))
                .andExpect(result().isProblem(403, TOKEN_VERIFICATION_FAILED.getType(),
                        "TokenVerificationFailed", "emailVerificationToken"));

        user = userRepository.findById(user.getId()).orElseThrow();
        assertThat(user.getRoles()).isEqualTo(Set.of(Role.UNVERIFIED));
    }

}
