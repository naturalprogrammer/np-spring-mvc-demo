package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EmailChangeRequestProcessorTest {

    @Mock
    private BeanValidator validator;

    @Mock
    private CommonUtils commonUtils;

    @InjectMocks
    private EmailChangeRequestProcessor subject;

    private final User user = randomUser();
    private final UserEmailChangeRequest request = new UserEmailChangeRequest("foo", "bar", "yoy");
    private final Problem problem = randomProblem();

    @Test
    void shouldNot_processEmailChangeRequest_when_validationFails() {

        // given
        given(commonUtils.getUserId()).willReturn(Optional.of(user.getId()));
        given(validator.validate(request)).willReturn(Optional.of(problem));

        // when
        var possibleProblem = subject.process(request);

        // then
        assertThat(possibleProblem).hasValue(problem);
    }
}