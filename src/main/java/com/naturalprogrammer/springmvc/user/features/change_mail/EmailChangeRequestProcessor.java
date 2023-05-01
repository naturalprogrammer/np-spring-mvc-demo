package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Slf4j
@Service
@RequiredArgsConstructor
class EmailChangeRequestProcessor {

    private final BeanValidator validator;
    private final ValidatedEmailChangeRequestProcessor validatedEmailChangeRequestProcessor;
    private final CommonUtils commonUtils;

    public Optional<Problem> process(UserEmailChangeRequest request) {

        var userId = commonUtils.getUserId().orElseThrow();
        log.info("Processing email change request user {}: {}", userId, request);
        var trimmedRequest = request.trimmed();
        return validator
                .validate(trimmedRequest)
                .or(() -> validatedEmailChangeRequestProcessor.process(userId, trimmedRequest));
    }

}

@Slf4j
@Service
@RequiredArgsConstructor
class ValidatedEmailChangeRequestProcessor {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ObjectFactory<ProblemBuilder> problemComposer;
    private final PasswordEncoder passwordEncoder;

    public Optional<Problem> process(UUID userId, UserEmailChangeRequest request) {

        return userRepository.findById(userId)
                .map(user -> process(user, request))
                .orElseGet(() -> {
                    log.warn("User {} not found when trying to process {}", userId, request);
                    return Optional.of(userService.userNotFound(userId));
                });
    }

    private Optional<Problem> process(User user, UserEmailChangeRequest request) {

        if (notEqual(user.getEmail(), request.oldEmail())) {
            var problem = problemComposer.getObject()
                    .type(ProblemType.EMAIL_MISMATCH)
                    .detailMessage("email-mismatch-for-user", user.getId())
                    .error("oldPassword", ErrorCode.EMAIL_MISMATCH)
                    .build();

            return Optional.of(problem);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            var problem = problemComposer.getObject()
                    .type(ProblemType.PASSWORD_MISMATCH)
                    .detailMessage("password-mismatch-for-user", user.getId())
                    .error("oldPassword", ErrorCode.PASSWORD_MISMATCH)
                    .build();
            return Optional.of(problem);
        }

        user.setNewEmail(request.newEmail());
        userRepository.save(user);

        log.info("Processed email change request user {}: {}", user, request);
        return Optional.empty();
    }
}
