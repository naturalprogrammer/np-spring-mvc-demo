package com.naturalprogrammer.springmvc.user.features.verification;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
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

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockProblemBuilder;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static com.nimbusds.jwt.JWTClaimNames.SUBJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@ExtendWith(MockitoExtension.class)
class ValidatedUserVerifierTest {

    @Mock
    private ObjectFactory<ProblemBuilder> problemBuilder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private JweService jweService;

    @InjectMocks
    private ValidatedUserVerifier subject;

    private final User user = randomUser();
    private final UserVerificationRequest request = new UserVerificationRequest("foo");
    private final Problem problem = randomProblem();

    @Test
    void should_preventVerification_when_userIsNotSelfOrAdmin() {

        // given
        given(userService.isSelfOrAdmin(user.getId())).willReturn(false);
        given(userService.userNotFound(user.getId())).willReturn(problem);

        // when
        var either = subject.verify(user.getId(), request);

        // then
        assertThat(either.getLeft()).hasValue(problem);
    }

    @Test
    void should_preventVerification_when_userNotFound() {

        // given
        given(userService.isSelfOrAdmin(user.getId())).willReturn(true);
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        given(userService.userNotFound(user.getId())).willReturn(problem);

        // when
        var either = subject.verify(user.getId(), request);

        // then
        assertThat(either.getLeft()).hasValue(problem);
    }

    @ParameterizedTest
    @ValueSource(strings = {SUBJECT, EMAIL})
    void should_preventVerification_when_wrongClaims(String claimName) {

        // given
        var claims = new JWTClaimsSet.Builder()
                .subject(user.getIdStr())
                .claim(EMAIL, user.getEmail())
                .claim(claimName, UUID.randomUUID().toString())
                .build();

        given(userService.isSelfOrAdmin(user.getId())).willReturn(true);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(jweService.parseToken(request.emailVerificationToken())).willReturn(Either.right(claims));
        mockProblemBuilder(problemBuilder);

        // when
        var either = subject.verify(user.getId(), request);

        // then
        assertThat(either.isLeft()).isTrue();
        var problem = either.getLeft().orElseThrow();
        assertThat(problem.id()).isNotBlank();
        assertThat(problem.type()).isEqualTo(ProblemType.TOKEN_VERIFICATION_FAILED.getType());
        assertThat(problem.title()).isEqualTo(ProblemType.TOKEN_VERIFICATION_FAILED.getTitle());
        assertThat(problem.status()).isEqualTo(ProblemType.TOKEN_VERIFICATION_FAILED.getStatus().value());
        assertThat(problem.detail()).isEqualTo(user.toString());
        assertThat(problem.instance()).isNull();
        assertThat(problem.errors()).isEmpty();
    }

}