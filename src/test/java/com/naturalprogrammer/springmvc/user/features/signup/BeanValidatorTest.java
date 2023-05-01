package com.naturalprogrammer.springmvc.user.features.signup;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockProblemBuilder;
import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockValidator;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.RANDOM_USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BeanValidatorTest {

    @Mock
    private LocalValidatorFactoryBean validator;

    @Mock
    private ObjectFactory<ProblemBuilder> problemComposer;

    @InjectMocks
    private BeanValidator subject;

    @BeforeEach
    void setUp() {
        mockValidator(validator);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"        ", "99999999", "AA99aa!", "AAA99aaa"})
    void should_preventSignup_when_passwordIsInvalid(String password) {

        // given
        var request = new SignupRequest(null, password, null, null);
        mockProblemBuilder(problemComposer);

        // when
        var possibleProblem = subject.validate(request);

        // then
        assertThat(possibleProblem).isNotEmpty();
        var problem = possibleProblem.orElseThrow();
        assertThat(problem.id()).isNotBlank();
        assertThat(problem.type()).isEqualTo(INVALID_DATA.getType());
        assertThat(problem.title()).isEqualTo(ProblemType.INVALID_DATA.getTitle());
        assertThat(problem.status()).isEqualTo(ProblemType.INVALID_DATA.getStatus().value());
        assertThat(problem.detail()).isEqualTo(request.toString());
        assertThat(problem.instance()).isNull();

        assertThat(problem.errors()).isNotEmpty();
        var possibleError = problem.errors().stream().filter(e -> e.field().equals("password")).findAny();
        assertThat(possibleError).isNotEmpty();
        var error = possibleError.orElseThrow();
        assertThat(error.code()).isEqualTo("invalid");
        assertThat(error.field()).isEqualTo("password");
        assertThat(error.message()).isEqualTo("Password must have least 1 upper, lower, special characters and digit, min 8 chars, max 50 chars");
    }

    @Test
    void should_beNoProblem_when_valid() {

        // given
        var request = new SignupRequest("email@example.com", RANDOM_USER_PASSWORD, "Some name", null);

        // when
        var possibleProblem = subject.validate(request);

        // then
        assertThat(possibleProblem).isEmpty();
    }
}