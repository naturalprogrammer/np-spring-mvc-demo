package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.error.*;
import com.naturalprogrammer.springmvc.common.jwt.JweService;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.EMAIL_CHANGE;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
class EmailChanger {

    private final CommonUtils commonUtils;
    private final BeanValidator validator;
    private final ValidatedEmailChanger validatedEmailChanger;

    public Optional<Problem> changeEmail(UserEmailChangeVerificationRequest request) {

        var userId = commonUtils.getUserId().orElseThrow();
        log.info("Changing email for user {} with {}", userId, request);
        return validator
                .validate(request)
                .or(() -> validatedEmailChanger.changeEmail(userId, request));
    }
}

@Slf4j
@Service
@RequiredArgsConstructor
class ValidatedEmailChanger {

    private final UserRepository userRepository;
    private final JweService jweService;
    private final ObjectFactory<ProblemBuilder> problemBuilder;
    private final Clock clock;

    public Optional<Problem> changeEmail(UUID userId, UserEmailChangeVerificationRequest request) {

        var user = userRepository.findById(userId).orElseThrow();

        return jweService
                .parseToken(request.emailVerificationToken())
                .fold(
                        problemType -> Optional.of(problemBuilder.getObject()
                                .type(problemType)
                                .detail(request.emailVerificationToken())
                                .error("emailVerificationToken", ErrorCode.TOKEN_VERIFICATION_FAILED)
                                .build()),
                        claims -> changeEmail(user, claims)
                );
    }

    private Optional<Problem> changeEmail(User user, JWTClaimsSet claims) {

        if (notEqual(claims.getSubject(), user.getIdStr()) ||
                notEqual(claims.getClaim(PURPOSE), EMAIL_CHANGE.name()) ||
                notEqual(claims.getClaim(EMAIL), user.getNewEmail()))
            return Optional.of(problemBuilder.getObject().build(ProblemType.TOKEN_VERIFICATION_FAILED, user.toString()));

        if (userRepository.existsByEmail(user.getNewEmail()))
            return Optional.of(problemBuilder.getObject()
                    .type(ProblemType.USED_EMAIL)
                    .detailMessage("used-given-email", user.getNewEmail())
                    .errors(emptyList())
                    .build());

        user.setEmail(user.getNewEmail());
        user.setNewEmail(null);
        user.resetTokensValidFrom(clock);
        userRepository.save(user);
        return Optional.empty();
    }
}
