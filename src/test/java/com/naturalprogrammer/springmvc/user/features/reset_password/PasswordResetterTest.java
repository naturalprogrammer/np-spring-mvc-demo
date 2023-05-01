package com.naturalprogrammer.springmvc.user.features.reset_password;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.common.jwt.JwtPurpose;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jbock.util.Either;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Optional;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockProblemBuilder;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@ExtendWith(MockitoExtension.class)
class PasswordResetterTest {

    @Mock
    private BeanValidator validator;

    @Mock
    private JweService jweService;

    @Mock
    private ObjectFactory<ProblemBuilder> problemBuilder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PasswordResetter subject;

    private final ResetPasswordRequest request = new ResetPasswordRequest(UUID.randomUUID().toString(), "NewPassword9!");
    private final Problem problem = randomProblem();

    @Test
    void shouldNot_resetPassword_when_invalidRequest() {

        // given
        given(validator.validate(request)).willReturn(Optional.of(problem));

        // when
        var possibleProblem = subject.reset(request);

        // then
        assertThat(possibleProblem).hasValue(problem);

    }

    @Test
    void shouldNot_resetPassword_when_invalidToken() {

        // given
        given(validator.validate(request)).willReturn(Optional.empty());
        given(jweService.parseToken(request.token())).willReturn(Either.left(ProblemType.EXPIRED_JWT));
        mockProblemBuilder(problemBuilder);

        // when
        var possibleProblem = subject.reset(request);

        // then
        assertThat(possibleProblem).isNotEmpty();
        var problem = possibleProblem.orElseThrow();
        assertThat(problem.type()).isEqualTo(ProblemType.EXPIRED_JWT.getType());
        assertThat(problem.detail()).isEqualTo(request.toString());
    }

    @Test
    void shouldNot_resetPassword_when_tokenHasWrongPurpose() {

        // given
        given(validator.validate(request)).willReturn(Optional.empty());
        given(jweService.parseToken(request.token())).willReturn(Either.right(
                new JWTClaimsSet.Builder().claim(PURPOSE, JwtPurpose.AUTH.name()).build()));
        mockProblemBuilder(problemBuilder);

        // when
        var possibleProblem = subject.reset(request);

        // then
        assertThat(possibleProblem).isNotEmpty();
        var problem = possibleProblem.orElseThrow();
        assertThat(problem.type()).isEqualTo(ProblemType.TOKEN_VERIFICATION_FAILED.getType());
        assertThat(problem.detail()).isEqualTo(request.toString());
    }

    @Test
    void shouldNot_resetPassword_when_userNotFound() {

        // given
        var userId = UUID.randomUUID();
        given(validator.validate(request)).willReturn(Optional.empty());
        given(jweService.parseToken(request.token())).willReturn(Either.right(
                new JWTClaimsSet.Builder()
                        .subject(userId.toString())
                        .claim(PURPOSE, JwtPurpose.FORGOT_PASSWORD.name()).build()));
        given(userService.userNotFound(userId)).willReturn(problem);

        // when
        var possibleProblem = subject.reset(request);

        // then
        assertThat(possibleProblem).hasValue(problem);
    }

    @Test
    void shouldNot_resetPassword_when_emailHasChanged() {

        // given
        var user = randomUser();
        var oldEmail = "old" + user.getEmail();
        given(validator.validate(request)).willReturn(Optional.empty());
        given(jweService.parseToken(request.token())).willReturn(Either.right(
                new JWTClaimsSet.Builder()
                        .subject(user.getIdStr())
                        .claim(EMAIL, oldEmail)
                        .claim(PURPOSE, JwtPurpose.FORGOT_PASSWORD.name()).build()));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userService.userNotFound(oldEmail)).willReturn(problem);

        // when
        var possibleProblem = subject.reset(request);

        // then
        assertThat(possibleProblem).hasValue(problem);
    }

}