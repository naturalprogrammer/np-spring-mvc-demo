package com.naturalprogrammer.springmvc.user.features.forgot_password;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordInitiatorTest {

    @Mock
    private BeanValidator validator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ForgotPasswordMailSender forgotPasswordMailSender;

    @InjectMocks
    private ForgotPasswordInitiator subject;

    @Test
    void should_initiateForgotPassword() {

        // given
        var user = randomUser();
        var request = new ForgotPasswordRequest(user.getEmail());
        given(validator.validate(request)).willReturn(Optional.empty());
        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));

        // when
        subject.initiate(request);

        // then
        verify(forgotPasswordMailSender).send(user);
    }

    @Test
    void shouldNot_initiateForgotPassword_when_validationFails() {

        // given
        var request = new ForgotPasswordRequest("");
        var problem = randomProblem();
        given(validator.validate(request)).willReturn(Optional.of(problem));

        // when
        var possibleProblem = subject.initiate(request);

        // then
        assertThat(possibleProblem).hasValue(problem);
        verify(forgotPasswordMailSender, never()).send(any());
    }

    @Test
    void should_silentlyNotInitiateForgotPasswordBut_when_userNotFound() {

        // given
        var user = randomUser();
        var request = new ForgotPasswordRequest(user.getEmail());
        given(validator.validate(request)).willReturn(Optional.empty());
        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

        // when
        var possibleProblem = subject.initiate(request);

        // then
        assertThat(possibleProblem).isEmpty();
        verify(forgotPasswordMailSender, never()).send(any());
    }

}