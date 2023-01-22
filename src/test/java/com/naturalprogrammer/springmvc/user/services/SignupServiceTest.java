package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.SignupService.Result.Error;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;
import java.util.Locale;
import java.util.Set;

import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_SIGNUP;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockValidator;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private LocalValidatorFactoryBean validator;

    @Mock
    private ProblemComposer problemComposer;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private Clock clock;

    @InjectMocks
    private SignupService subject;

    @Nested
    class ValidationTest {

        private final Problem problem = randomProblem();

        @Captor
        private ArgumentCaptor<Set<ConstraintViolation<SignupRequest>>> violationsCaptor;

        @BeforeEach
        void setUp() {
            mockValidator(validator);
            given(problemComposer.compose(any(), any(), anySet())).willReturn(problem);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"        ", "99999999", "AA99aa!", "AAA99aaa"})
        void should_preventSignup_when_passwordIsInvalid(String password) {

            // given
            var request = new SignupRequest(null, password, null);

            // when
            var result = subject.signup(request, Locale.ENGLISH);

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
            assertThat(result).isInstanceOf(Error.class);
            assertThat(((Error) result).problem()).isEqualTo(problem);
            verifyNoMoreInteractions(userRepository, userService, clock, passwordEncoder);
        }
    }
}