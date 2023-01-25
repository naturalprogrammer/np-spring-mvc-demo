package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.dto.UserDisplayNameEditRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayNameEditor {

    private final ProblemComposer problemComposer;

    public DisplayNameEditor.Result edit(UUID id, UserDisplayNameEditRequest request) {

        log.info("Editing display name of user {} to {}", id, request);
        log.info("Authentication {}", SecurityContextHolder.getContext().getAuthentication());
        return new Result.Error(problemComposer.compose(ProblemType.GENERIC_ERROR, ""));
    }

    public sealed interface Result {
        record Success(UserResource response) implements DisplayNameEditor.Result {
        }

        record Error(Problem problem) implements DisplayNameEditor.Result {
        }
    }
}
