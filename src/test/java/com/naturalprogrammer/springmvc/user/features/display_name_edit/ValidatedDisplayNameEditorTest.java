package com.naturalprogrammer.springmvc.user.features.display_name_edit;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.naturalprogrammer.springmvc.helpers.MyTestUtils.randomProblem;
import static com.naturalprogrammer.springmvc.user.UserTestUtils.randomUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ValidatedDisplayNameEditorTest {

    @Mock
    private ProblemComposer problemComposer;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ValidatedDisplayNameEditor subject;

    private final User user = randomUser();
    private final Problem problem = randomProblem();
    private final UserDisplayNameEditRequest request = new UserDisplayNameEditRequest("Some new Name");

    @BeforeEach
    void setUp() {
        given(problemComposer.composeMessage(ProblemType.NOT_FOUND, "user-not-found", user.getId())).willReturn(problem);
    }

    @Test
    void should_preventEditingDisplayName_when_userNotFound() {

        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // when
        var either = subject.edit(user.getId(), request);

        // then
        assertThat(either.getLeft()).hasValue(problem);
    }

    @Test
    void should_preventVerification_when_userIsNotSelfOrAdmin() {

        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userService.isSelfOrAdmin(user)).willReturn(false);

        // when
        var either = subject.edit(user.getId(), request);

        // then
        assertThat(either.getLeft()).hasValue(problem);
    }

}