package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.error.ProblemType.INVALID_DATA;
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
                .validate(trimmedRequest, INVALID_DATA)
                .or(() -> validatedEmailChangeRequestProcessor.process(userId, trimmedRequest));
    }

}

@Slf4j
@Service
@RequiredArgsConstructor
class ValidatedEmailChangeRequestProcessor {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ProblemComposer problemComposer;
    private final MessageGetter messageGetter;
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
            var problem = problemComposer.compose(
                    ProblemType.EMAIL_MISMATCH,
                    messageGetter.getMessage("email-mismatch-for-user", user.getId()),
                    ErrorCode.EMAIL_MISMATCH,
                    "oldEmail"
            );
            return Optional.of(problem);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            var problem = problemComposer.compose(
                    ProblemType.PASSWORD_MISMATCH,
                    messageGetter.getMessage("password-mismatch-for-user", user.getId()),
                    ErrorCode.PASSWORD_MISMATCH,
                    "password"
            );
            return Optional.of(problem);
        }

        user.setNewEmail(request.newEmail());
        userRepository.save(user);

        log.info("Processed email change request user {}: {}", user, request);
        return Optional.empty();
    }
}
