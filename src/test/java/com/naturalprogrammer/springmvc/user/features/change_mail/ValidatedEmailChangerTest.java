package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Optional;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.error.ErrorCode.TOKEN_VERIFICATION_FAILED;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.EXPIRED_JWT;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.EMAIL_CHANGE;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockProblemBuilder;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.FAKER;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.nimbusds.jwt.JWTClaimNames.SUBJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@ExtendWith(MockitoExtension.class)
class ValidatedEmailChangerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JweService jweService;

    @Mock
    private ObjectFactory<ProblemBuilder> problemBuilder;

    @InjectMocks
    private ValidatedEmailChanger subject;

    private final User user = randomUser();
    private final UserEmailChangeVerificationRequest request =
            new UserEmailChangeVerificationRequest(UUID.randomUUID().toString());

    @BeforeEach
    void setUp() {
        user.setNewEmail(FAKER.internet().emailAddress());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    }

    @Test
    void Should_NotChangeEmail_When_VerificationTokenIsInvalid() {

        // given
        given(jweService.parseToken(request.emailVerificationToken()))
                .willReturn(Either.left(EXPIRED_JWT));
        mockProblemBuilder(problemBuilder);

        // when
        var possibleProblem = subject.changeEmail(user.getId(), request);

        // then
        assertThat(possibleProblem).isPresent();
        var problem = possibleProblem.orElseThrow();
        var softly = new SoftAssertions();
        softly.assertThat(problem.type()).isEqualTo(EXPIRED_JWT.getType());
        softly.assertThat(problem.detail()).isEqualTo(request.emailVerificationToken());
        softly.assertThat(problem.errors()).hasSize(1);
        var error = problem.errors().get(0);
        softly.assertThat(error.field()).isEqualTo("emailVerificationToken");
        softly.assertThat(error.code()).isEqualTo(TOKEN_VERIFICATION_FAILED.getCode());
        softly.assertAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {SUBJECT, EMAIL, PURPOSE})
    void Should_NotChangeEmail_When_ClaimsDoNotMatch(String claimName) {

        // given
        var claims = new JWTClaimsSet.Builder()
                .subject(user.getIdStr())
                .claim(EMAIL, user.getNewEmail())
                .claim(PURPOSE, EMAIL_CHANGE.name())
                .claim(claimName, UUID.randomUUID().toString())
                .build();
        given(jweService.parseToken(request.emailVerificationToken())).willReturn(Either.right(claims));
        mockProblemBuilder(problemBuilder);

        // when
        var possibleProblem = subject.changeEmail(user.getId(), request);

        // then
        assertThat(possibleProblem).isPresent();
        var problem = possibleProblem.orElseThrow();
        var softly = new SoftAssertions();
        softly.assertThat(problem.type()).isEqualTo(ProblemType.TOKEN_VERIFICATION_FAILED.getType());
        softly.assertThat(problem.detail()).contains(user.getIdStr());
        softly.assertThat(problem.errors()).isEmpty();
        softly.assertAll();
    }

    @Test
    void Should_NotChangeEmail_When_NewEmailAlreadyUsed() {

        var claims = new JWTClaimsSet.Builder()
                .subject(user.getIdStr())
                .claim(EMAIL, user.getNewEmail())
                .claim(PURPOSE, EMAIL_CHANGE.name())
                .build();
        given(jweService.parseToken(request.emailVerificationToken())).willReturn(Either.right(claims));
        given(userRepository.existsByEmail(user.getNewEmail())).willReturn(true);
        mockProblemBuilder(problemBuilder);

        // when
        var possibleProblem = subject.changeEmail(user.getId(), request);

        // then
        assertThat(possibleProblem).isPresent();
        var problem = possibleProblem.orElseThrow();
        var softly = new SoftAssertions();
        softly.assertThat(problem.type()).isEqualTo(ProblemType.USED_EMAIL.getType());
        softly.assertThat(problem.detail()).contains(user.getNewEmail());
        softly.assertThat(problem.errors()).isEmpty();
        softly.assertAll();
    }
}