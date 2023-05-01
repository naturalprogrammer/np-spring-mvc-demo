package com.naturalprogrammer.springmvc.user.features.reset_password;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

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
    private final ObjectFactory<ProblemBuilder> problemBuilder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public Optional<Problem> reset(ResetPasswordRequest request) {

        log.info("Resetting password for {}", request);
        var trimmedRequest = request.trimmed();
        return validator
                .validate(trimmedRequest)
                .or(() -> resetValidatedPassword(trimmedRequest));
    }

    private Optional<Problem> resetValidatedPassword(ResetPasswordRequest request) {

        return jweService
                .parseToken(request.token())
                .fold(problemType -> {
                    var problem = problemBuilder.getObject().build(
                            problemType,
                            request.toString());
                    return Optional.of(problem);
                }, claims -> resetPassword(request, claims));
    }

    @SneakyThrows
    private Optional<Problem> resetPassword(ResetPasswordRequest request, JWTClaimsSet claims) {

        if (notEqual(claims.getClaim(PURPOSE), FORGOT_PASSWORD.name())) {
            log.warn("Received token with invalid purpose while resetting password {}", claims);
            return Optional.of(problemBuilder.getObject().build(ProblemType.TOKEN_VERIFICATION_FAILED, request.toString()));
        }

        var userId = UUID.fromString(claims.getSubject());
        var email = claims.getStringClaim(EMAIL);

        var possibleUser = userRepository.findById(userId);
        if (possibleUser.isEmpty())
            return Optional.of(userService.userNotFound(userId));
        return possibleUser.flatMap(user -> resetPassword(user, email, request.newPassword()));
    }

    private Optional<Problem> resetPassword(User user, String expectedEmail, String newPassword) {

        if (notEqual(user.getEmail(), expectedEmail)) {
            log.warn("While resetting password, email already changed from {} to {}", expectedEmail, user);
            return Optional.of(userService.userNotFound(expectedEmail));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.resetTokensValidFrom(clock);
        userRepository.save(user);
        return Optional.empty();
    }
}
