package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

import java.util.UUID;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.mockProblemBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessTokenCreatorTest {

    @Mock
    private UserService userService;

    @Mock
    private ObjectFactory<ProblemBuilder> problemBuilder;

    @InjectMocks
    private AccessTokenCreator subject;

    @Test
    void shouldNot_createAccessToken_when_notSelfOrAdmin() {

        // given
        var userId = UUID.randomUUID();
        given(userService.isSelfOrAdmin(userId)).willReturn(false);
        mockProblemBuilder(problemBuilder);

        // when
        var either = subject.create(userId);

        // then
        assertThat(either.isLeft()).isTrue();
        var problem = either.getLeft().orElseThrow();
        assertThat(problem.type()).isEqualTo(ProblemType.NOT_FOUND.getType());
    }
}