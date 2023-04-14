package com.naturalprogrammer.springmvc.user.features.change_password;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
    private ProblemComposer problemComposer;

    @Mock
    private MessageGetter messageGetter;

    @InjectMocks
    private PasswordChanger subject;

    private final User user = randomUser();
    private final ChangePasswordRequest request = new ChangePasswordRequest("foo", "bar");
    private final Problem problem = randomProblem();

    private void mockGetAuthentication() {
        var authentication = mock(Authentication.class);
        given(authentication.getName()).willReturn(user.getIdStr());
        given(commonUtils.getAuthentication()).willReturn(Optional.of(authentication));
    }

    @Test
    void shouldNot_changePassword_when_validationFails() {

        // given
        mockGetAuthentication();
        given(validator.validate(request, ProblemType.INVALID_DATA)).willReturn(Optional.of(problem));

        // when
        var possibleProblem = subject.changePassword(request);

        // then
        assertThat(possibleProblem).hasValue(problem);
    }

    @Test
    void shouldNot_changePassword_when_oldPasswordDoesNotMatch() {

        // given
        mockGetAuthentication();
        given(validator.validate(request, ProblemType.INVALID_DATA)).willReturn(Optional.empty());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.oldPassword(), user.getPassword())).willReturn(false);

        var message = "Old password doesn't match!";
        given(messageGetter.getMessage("password-mismatch-for-user", user.getId())).willReturn(message);
        given(problemComposer.compose(ProblemType.PASSWORD_MISMATCH, message, ErrorCode.PASSWORD_MISMATCH, "oldPassword"))
                .willReturn(problem);

        // when
        var possibleProblem = subject.changePassword(request);

        // then
        assertThat(possibleProblem).hasValue(problem);
    }

}