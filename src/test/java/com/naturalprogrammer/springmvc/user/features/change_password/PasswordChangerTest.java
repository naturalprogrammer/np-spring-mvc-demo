package com.naturalprogrammer.springmvc.user.features.change_password;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockProblemBuilder;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PasswordChangerTest {

    @Mock
    private BeanValidator validator;

    @Mock
    private CommonUtils commonUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectFactory<ProblemBuilder> problemBuilder;

    @InjectMocks
    private PasswordChanger subject;

    private final User user = randomUser();
    private final ChangePasswordRequest request = new ChangePasswordRequest("foo", "bar");
    private final Problem problem = randomProblem();

    private void mockGetAuthentication() {
        given(commonUtils.getUserId()).willReturn(Optional.of(user.getId()));
    }

    @Test
    void shouldNot_changePassword_when_validationFails() {

        // given
        mockGetAuthentication();
        given(validator.validate(request)).willReturn(Optional.of(problem));

        // when
        var possibleProblem = subject.changePassword(request);

        // then
        assertThat(possibleProblem).hasValue(problem);
    }

    @Test
    void shouldNot_changePassword_when_oldPasswordDoesNotMatch() {

        // given
        mockGetAuthentication();
        given(validator.validate(request)).willReturn(Optional.empty());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.oldPassword(), user.getPassword())).willReturn(false);
        mockProblemBuilder(problemBuilder);

        // when
        var possibleProblem = subject.changePassword(request);

        // then
        assertThat(possibleProblem).isNotEmpty();
        var problem = possibleProblem.orElseThrow();
        assertThat(problem.id()).isNotBlank();
        assertThat(problem.type()).isEqualTo(ProblemType.PASSWORD_MISMATCH.getType());
        assertThat(problem.title()).isEqualTo(ProblemType.PASSWORD_MISMATCH.getTitle());
        assertThat(problem.status()).isEqualTo(ProblemType.PASSWORD_MISMATCH.getStatus().value());
        assertThat(problem.detail()).isEqualTo("password-mismatch-for-user" + user.getId());
        assertThat(problem.instance()).isNull();
        assertThat(problem.errors()).hasSize(1);
        var error = problem.errors().get(0);
        assertThat(error.code()).isEqualTo(ErrorCode.PASSWORD_MISMATCH.getCode());
        assertThat(error.field()).isEqualTo("oldPassword");
        assertThat(error.message()).isEqualTo(ErrorCode.PASSWORD_MISMATCH.getMessage());
    }

}