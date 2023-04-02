package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProblemComposer problemComposer;
    private final AuthTokenCreator authTokenCreator;

    public Either<Problem, AuthTokensResource> login(LoginRequest loginRequest) {

        log.info("Creating AuthToken for {}", loginRequest);
        return userRepository
                .findByEmail(loginRequest.email())
                .map(user -> createResourceToken(user, loginRequest))
                .orElseGet(() -> Either.left(problemComposer.compose(ProblemType.WRONG_CREDENTIALS, loginRequest.toString())));
    }

    private Either<Problem, AuthTokensResource> createResourceToken(User user, LoginRequest loginRequest) {
        return passwordEncoder.matches(loginRequest.password(), user.getPassword())
                ? Either.right(authTokenCreator.create(user.getIdStr(), loginRequest.resourceTokenValidForMillis()))
                : Either.left(problemComposer.compose(ProblemType.WRONG_CREDENTIALS, loginRequest.toString()));
    }
}
