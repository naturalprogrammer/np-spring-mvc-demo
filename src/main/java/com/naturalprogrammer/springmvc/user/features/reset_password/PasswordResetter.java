package com.naturalprogrammer.springmvc.user.features.reset_password;

import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.FORGOT_PASSWORD;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
class PasswordResetter {

    private final BeanValidator validator;
    private final JweService jweService;
    private final ProblemComposer problemComposer;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public Optional<Problem> reset(ResetPasswordRequest request) {

        log.info("Resetting password for {}", request);
        var trimmedRequest = request.trimmed();
        return validator
                .validate(trimmedRequest, ProblemType.INVALID_DATA)
                .or(() -> resetValidatedPassword(trimmedRequest));
    }

    private Optional<Problem> resetValidatedPassword(ResetPasswordRequest request) {

        return jweService
                .parseToken(request.token())
                .fold(problemType -> {
                    var problem = problemComposer.compose(
                            problemType,
                            request.toString(),
                            ErrorCode.TOKEN_VERIFICATION_FAILED,
                            "token");
                    return Optional.of(problem);
                }, claims -> resetPassword(request, claims));
    }

    @SneakyThrows
    private Optional<Problem> resetPassword(ResetPasswordRequest request, JWTClaimsSet claims) {

        if (notEqual(claims.getClaim(PURPOSE), FORGOT_PASSWORD.name())) {
            log.warn("Received token with invalid purpose while resetting password {}", claims);
            return Optional.of(problemComposer.compose(ProblemType.TOKEN_VERIFICATION_FAILED, request.toString()));
        }

        var email = claims.getStringClaim(EMAIL);

        return userRepository
                .findByEmail(email)
                .flatMap(user -> {
                    resetPassword(user, request.newPassword());
                    return Optional.<Problem>empty();
                })
                .or(() -> Optional.of(userService.userNotFound(email)));
    }

    private void resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.resetTokensValidFrom(clock);
        userRepository.save(user);
    }
}
