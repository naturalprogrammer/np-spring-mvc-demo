package com.naturalprogrammer.springmvc.user.features.signup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JwsService;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.features.login.AuthScope;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.USED_EMAIL;
import static com.naturalprogrammer.springmvc.common.mail.LoggingMailSender.sentMails;
import static com.naturalprogrammer.springmvc.helpers.MyResultMatchers.result;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.RANDOM_USER_PASSWORD;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.ACCESS_TOKEN_VALID_MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.SCOPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SignupIntegrationTest extends AbstractIntegrationTest {

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
        var displayName = "Sanjay567 Patel336";
        var resourceTokenValidForMillis = DAYS.toMillis(10);
        var beginTime = Instant.now().truncatedTo(SECONDS);
        sentMails().clear();

        // when, then
        var response = mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
                        .content("""
                                   {
                                        "email" : "%s",
                                        "password" : "%s",
                                        "displayName" : "%s",
                                        "resourceTokenValidForMillis" : %d
                                   }
                                """.formatted(email, RANDOM_USER_PASSWORD, displayName, resourceTokenValidForMillis)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("email").value(email))
                .andExpect(jsonPath("displayName").value(displayName))
                .andExpect(jsonPath("locale").value("en-IN"))
                .andExpect(jsonPath("roles", hasSize(1)))
                .andExpect(jsonPath("roles", contains(Role.UNVERIFIED.name())))
                .andExpect(jsonPath("authTokens.resourceToken").isString())
                .andExpect(jsonPath("authTokens.accessToken").isString())
                .andExpect(jsonPath("authTokens.resourceTokenValidUntil").isString())
                .andExpect(jsonPath("authTokens.accessTokenValidUntil").isString())
                .andReturn()
                .getResponse();

        var endTime = Instant.now().truncatedTo(SECONDS);
        var userResource = mapper.readValue(response.getContentAsString(), UserResource.class);

        User user = userRepository.findById(userResource.id()).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(RANDOM_USER_PASSWORD, user.getPassword())).isTrue();
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getLocale().toLanguageTag()).isEqualTo("en-IN");
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles()).contains(Role.UNVERIFIED);
        assertThat(user.getNewEmail()).isNull();
        assertThat(user.getTokensValidFrom()).isAfterOrEqualTo(beginTime);
        assertThat(user.getTokensValidFrom()).isBeforeOrEqualTo(endTime);

        var authTokens = userResource.authTokens();
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
        assertThat(response.getHeader(LOCATION)).isEqualTo(USERS + "/" + userResource.id());

        assertClaims(
                beginTime,
                endTime,
                userResource.id(),
                jwsService.parseToken(userResource.authTokens().accessToken()),
                ACCESS_TOKEN_VALID_MILLIS,
                "normal"
        );
        assertClaims(
                beginTime,
                endTime,
                userResource.id(),
                jwsService.parseToken(userResource.authTokens().resourceToken()),
                resourceTokenValidForMillis,
                AuthScope.AUTH_TOKENS.getValue()
        );

        assertThat(sentMails()).hasSize(1);
        var mailData = sentMails().get(0);
        assertThat(mailData.to()).isEqualTo(email);
        assertThat(mailData.bodyHtml()).contains(displayName);
        assertThat(mailData.subject()).isEqualTo("Please verify your email");
    }

    public static void assertClaims(
            Instant beginTime,
            Instant endTime,
            UUID userId,
            Either<ProblemType, JWTClaimsSet> parseResult,
            long tokenValidForMillis,
            String scope
    ) throws ParseException {
        assertThat(parseResult.isRight()).isTrue();
        JWTClaimsSet claims = parseResult.getRight().orElseThrow();
        assertThat(claims.getIssuer()).isEqualTo("http://www.example.com");
        assertThat(claims.getIssueTime()).isAfterOrEqualTo(beginTime);
        assertThat(claims.getIssueTime()).isBeforeOrEqualTo(endTime);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getAudience()).isEqualTo(List.of("http://www.example.com"));
        assertThat(claims.getExpirationTime()).isAfterOrEqualTo(beginTime.plusMillis(tokenValidForMillis));
        assertThat(claims.getExpirationTime()).isBeforeOrEqualTo(endTime.plusMillis(tokenValidForMillis));
        assertThat(claims.getStringClaim(SCOPE)).isEqualTo(scope);
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
                                        "password" : "%s",
                                        "displayName" : "  "
                                   }
                                """.formatted(RANDOM_USER_PASSWORD)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
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
                                        "password" : "%s",
                                        "displayName" : "Sanjay457 Patel983"
                                   }
                                """.formatted(user.getEmail(), RANDOM_USER_PASSWORD)))
                .andExpect(result().isProblem(409, USED_EMAIL.getType(),
                        "UsedEmail", "email"))
                .andExpect(jsonPath("title").value("Email already used"))
                .andExpect(jsonPath("status").value("409"))
                .andExpect(jsonPath("errors[0].message").value("Email already used"));
    }

}