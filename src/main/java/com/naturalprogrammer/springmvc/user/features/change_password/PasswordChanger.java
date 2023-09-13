package com.naturalprogrammer.springmvc.user.features.change_password;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordChanger {

    private final BeanValidator validator;
    private final CommonUtils commonUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectFactory<ProblemBuilder> problemBuilder;
    private final Clock clock;

    public Optional<Problem> changePassword(ChangePasswordRequest request) {

        var userId = commonUtils.getUserId().orElseThrow();

        log.info("Changing password for user {}", userId);
        var trimmedRequest = request.trimmed();
        return validator
                .validate(trimmedRequest)
                .or(() -> changeValidatedPassword(userId, trimmedRequest));
    }

    private Optional<Problem> changeValidatedPassword(UUID userId, ChangePasswordRequest request) {

        var user = userRepository.findById(userId).orElseThrow();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            var problem = problemBuilder.getObject()
                    .type(ProblemType.PASSWORD_MISMATCH)
                    .detailMessage("password-mismatch-for-user", userId)
                    .error("oldPassword", ErrorCode.PASSWORD_MISMATCH)
                    .build();
            return Optional.of(problem);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.resetTokensValidFrom(clock);
        userRepository.save(user);
        return Optional.empty();
    }
}
