package com.naturalprogrammer.springmvc.user.features.resend_verification;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.features.verification.VerificationMailSender;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class VerificationMailReSender {

    private final UserService userService;
    private final UserRepository userRepository;
    private final VerificationMailSender verificationMailSender;
    private final ObjectFactory<ProblemBuilder> problemBuilder;

    public Optional<Problem> resend(UUID userId) {

        if (!userService.isSelfOrAdmin(userId)) {
            log.warn("User {} is not self or admin when trying to create verification token", userId);
            return Optional.of(userService.userNotFound(userId));
        }
        return userRepository
                .findById(userId)
                .map(this::resend)
                .orElseGet(() -> {
                    log.warn("User {} not found when trying to create verification token", userId);
                    return Optional.of(userService.userNotFound(userId));
                });
    }

    public Optional<Problem> resend(User user) {

        if (user.hasRoles(Role.VERIFIED)) {
            log.warn("User {} already verified trying to create verification token", user);
            var problem = problemBuilder.getObject()
                    .type(ProblemType.USER_ALREADY_VERIFIED)
                    .detailMessage("given-user-already-verified", user.getId())
                    .build();
            return Optional.of(problem);
        }
        verificationMailSender.send(user);
        return Optional.empty();
    }
}
