package com.naturalprogrammer.springmvc.user.features.signup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.error.Problem;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_SIGNUP;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.USED_EMAIL;
import static com.naturalprogrammer.springmvc.common.mail.LoggingMailSender.sentMails;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator.ACCESS_TOKEN_VALID_MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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
                                """.formatted(email, password, displayName, resourceTokenValidForMillis)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(UserResource.CONTENT_TYPE))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("email").value(email))
                .andExpect(jsonPath("displayName").value(displayName))
                .andExpect(jsonPath("locale").value("en-IN"))
                .andExpect(jsonPath("roles", hasSize(1)))
                .andExpect(jsonPath("roles", contains(Role.UNVERIFIED.name())))
                .andExpect(jsonPath("authToken.resourceToken").isString())
                .andExpect(jsonPath("authToken.accessToken").isString())
                .andExpect(jsonPath("authToken.resourceTokenValidUntil").isString())
                .andExpect(jsonPath("authToken.accessTokenValidUntil").isString())
                .andReturn()
                .getResponse();

        var endTime = Instant.now().truncatedTo(SECONDS);
        var userResource = mapper.readValue(response.getContentAsString(), UserResource.class);

        User user = userRepository.findById(userResource.id()).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getLocale().toLanguageTag()).isEqualTo("en-IN");
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles()).contains(Role.UNVERIFIED);
        assertThat(user.getNewEmail()).isNull();
        assertThat(user.getTokensValidFrom()).isAfterOrEqualTo(beginTime);
        assertThat(user.getTokensValidFrom()).isBeforeOrEqualTo(endTime);

        var authToken = userResource.authToken();
        assertThat(authToken.resourceToken()).isNotNull();
        assertThat(authToken.accessToken()).isNotNull();
        assertThat(authToken.resourceTokenValidUntil()).isAfterOrEqualTo(
                beginTime.plusMillis(resourceTokenValidForMillis));
        assertThat(authToken.resourceTokenValidUntil()).isBeforeOrEqualTo(
                endTime.plusMillis(resourceTokenValidForMillis));
        assertThat(authToken.accessTokenValidUntil()).isAfterOrEqualTo(
                beginTime.plusMillis(ACCESS_TOKEN_VALID_MILLIS));
        assertThat(authToken.accessTokenValidUntil()).isBeforeOrEqualTo(
                endTime.plusMillis(ACCESS_TOKEN_VALID_MILLIS));
        assertThat(response.getHeader(LOCATION)).isEqualTo(USERS + "/" + userResource.id());

        assertClaims(
                beginTime,
                endTime,
                userResource.id(),
                jwsService.parseToken(userResource.authToken().accessToken()),
                ACCESS_TOKEN_VALID_MILLIS,
                null
        );
        assertClaims(
                beginTime,
                endTime,
                userResource.id(),
                jwsService.parseToken(userResource.authToken().resourceToken()),
                resourceTokenValidForMillis,
                AuthScope.RESOURCE_TOKEN.getValue()
        );

        assertThat(sentMails()).hasSize(1);
        var mailData = sentMails().get(0);
        assertThat(mailData.to()).isEqualTo(email);
        assertThat(mailData.bodyHtml()).contains(displayName);
        assertThat(mailData.subject()).isEqualTo("Please verify your email");
    }

    private void assertClaims(
            Instant beginTime,
            Instant endTime,
            UUID userId,
            Either<ProblemType, JWTClaimsSet> parseResult,
            long tokenValidForMillis,
            String scope
    ) throws ParseException {
        assertThat(parseResult.isRight()).isTrue();
        JWTClaimsSet claims = parseResult.getRight().orElseThrow();
        assertThat(claims.getIssuer()).isEqualTo("https://www.my-super-site.example.com");
        assertThat(claims.getIssueTime()).isAfterOrEqualTo(beginTime);
        assertThat(claims.getIssueTime()).isBeforeOrEqualTo(endTime);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getAudience()).isEqualTo(List.of("https://www.my-super-site.example.com"));
        assertThat(claims.getExpirationTime()).isAfterOrEqualTo(beginTime.plusMillis(tokenValidForMillis));
        assertThat(claims.getExpirationTime()).isBeforeOrEqualTo(endTime.plusMillis(tokenValidForMillis));
        assertThat(claims.getStringClaim("scope")).isEqualTo(scope);
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