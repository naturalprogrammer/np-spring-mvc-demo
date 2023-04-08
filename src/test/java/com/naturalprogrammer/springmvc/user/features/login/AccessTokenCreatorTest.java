package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessTokenCreatorTest {

    @Mock
    private UserService userService;

    @Mock
    private ProblemComposer problemComposer;

    @InjectMocks
    private AccessTokenCreator subject;

    @Test
    void shouldNot_createAccessToken_when_notSelfOrAdmin() {

        // given
        var userId = UUID.randomUUID();
        given(userService.isSelfOrAdmin(userId)).willReturn(false);

        var problem = randomProblem();
        given(problemComposer.compose(eq(ProblemType.NOT_FOUND), any())).willReturn(problem);

        // when
        var either = subject.create(userId);

        // then
        assertThat(either.getLeft()).hasValue(problem);
    }
}