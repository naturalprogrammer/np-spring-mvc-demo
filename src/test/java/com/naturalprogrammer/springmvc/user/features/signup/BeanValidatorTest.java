package com.naturalprogrammer.springmvc.user.features.signup;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_SIGNUP;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockValidator;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BeanValidatorTest {

    @Mock
    private LocalValidatorFactoryBean validator;

    @Mock
    private ProblemComposer problemComposer;

    @InjectMocks
    private BeanValidator subject;

    @Captor
    private ArgumentCaptor<Set<ConstraintViolation<SignupRequest>>> violationsCaptor;

    private final Problem problem = randomProblem();

    @BeforeEach
    void setUp() {
        mockValidator(validator);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"        ", "99999999", "AA99aa!", "AAA99aaa"})
    void should_preventSignup_when_passwordIsInvalid(String password) {

        // given
        var request = new SignupRequest(null, password, null);
        given(problemComposer.compose(any(), any(), anySet())).willReturn(problem);

        // when
        var possibleProblem = subject.validate(request, INVALID_SIGNUP);

        // then
        verify(problemComposer).compose(
                eq(INVALID_SIGNUP),
                eq("SignupRequest{email='null', displayName='null'}"),
                violationsCaptor.capture()
        );

        var violations = violationsCaptor.getValue();
        var passwordViolation = violations
                .stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("password"))
                .findAny()
                .orElseThrow();

        assertThat(passwordViolation.getMessageTemplate()).isEqualTo("{com.naturalprogrammer.spring.invalid.password}");
        assertThat(passwordViolation.getMessage()).isEqualTo("Password must have least 1 upper, lower, special characters and digit, min 8 chars, max 50 chars");
        assertThat(possibleProblem).hasValue(problem);
    }

    @Test
    void should_beNoProblem_when_valid() {

        // given
        var request = new SignupRequest("email@example.com", "Password9!", "Some name");

        // when
        var possibleProblem = subject.validate(request, INVALID_SIGNUP);

        // then
        assertThat(possibleProblem).isEmpty();
    }
}