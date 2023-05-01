package com.naturalprogrammer.springmvc.user.features.resend_verification;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Optional;
import java.util.Set;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockProblemBuilder;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VerificationMailReSenderTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectFactory<ProblemBuilder> problemComposer;

    @InjectMocks
    private VerificationMailReSender subject;

    private final User user = randomUser();
    private final Problem problem = randomProblem();

    @Test
    void shouldNot_sendVerificationMail_when_notSelfOrAdmin() {

        // given
        given(userService.isSelfOrAdmin(user.getId())).willReturn(false);
        given(userService.userNotFound(user.getId())).willReturn(problem);

        // when
        var possibleProblem = subject.resend(user.getId());

        // then
        assertThat(possibleProblem).hasValue(problem);
    }

    @Test
    void shouldNot_sendVerificationMail_when_userNotFound() {

        // given
        given(userService.isSelfOrAdmin(user.getId())).willReturn(true);
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        given(userService.userNotFound(user.getId())).willReturn(problem);

        // when
        var possibleProblem = subject.resend(user.getId());

        // then
        assertThat(possibleProblem).hasValue(problem);
    }

    @Test
    void shouldNot_sendVerificationMail_when_userIsAlreadyVerified() {

        // given
        user.setRoles(Set.of(Role.VERIFIED));
        given(userService.isSelfOrAdmin(user.getId())).willReturn(true);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        mockProblemBuilder(problemComposer);

        // when
        var possibleProblem = subject.resend(user.getId());

        // then
        assertThat(possibleProblem).isNotEmpty();
        var problem = possibleProblem.orElseThrow();
        assertThat(problem.id()).isNotBlank();
        assertThat(problem.type()).isEqualTo(ProblemType.USER_ALREADY_VERIFIED.getType());
        assertThat(problem.title()).isEqualTo(ProblemType.USER_ALREADY_VERIFIED.getTitle());
        assertThat(problem.status()).isEqualTo(ProblemType.USER_ALREADY_VERIFIED.getStatus().value());
        assertThat(problem.detail()).isEqualTo("given-user-already-verified" + user.getId());
        assertThat(problem.instance()).isNull();
        assertThat(problem.errors()).isEmpty();
    }
}