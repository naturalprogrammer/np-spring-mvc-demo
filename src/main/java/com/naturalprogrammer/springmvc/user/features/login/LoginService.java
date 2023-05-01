package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import io.jbock.util.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final BeanValidator validator;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectFactory<ProblemBuilder> problemBuilder;
    private final AuthTokenCreator authTokenCreator;

    public Either<Problem, AuthTokensResource> login(LoginRequest request) {

        log.info("Creating AuthToken for {}", request);
        var trimmedRequest = request.trimmed();
        return validator.validateAndGet(trimmedRequest, () -> loginValidated(trimmedRequest));
    }

    private Either<Problem, AuthTokensResource> loginValidated(LoginRequest loginRequest) {

        return userRepository
                .findByEmail(loginRequest.email())
                .map(user -> createResourceToken(user, loginRequest))
                .orElseGet(() -> Either.left(problemBuilder.getObject().build(ProblemType.WRONG_CREDENTIALS, loginRequest.toString())));
    }

    private Either<Problem, AuthTokensResource> createResourceToken(User user, LoginRequest loginRequest) {
        return passwordEncoder.matches(loginRequest.password(), user.getPassword())
                ? Either.right(authTokenCreator.create(user.getIdStr(), loginRequest.resourceTokenValidForMillis()))
                : Either.left(problemBuilder.getObject().build(ProblemType.WRONG_CREDENTIALS, loginRequest.toString()));
    }
}
