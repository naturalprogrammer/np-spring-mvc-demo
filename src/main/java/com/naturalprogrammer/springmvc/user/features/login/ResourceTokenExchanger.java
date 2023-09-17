package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import com.naturalprogrammer.springmvc.user.services.UserService;
import io.jbock.util.Either;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.CommonUtils.deleteCookies;
import static com.naturalprogrammer.springmvc.config.sociallogin.HttpCookieOAuth2AuthorizationRequestRepository.CLIENT_ID_COOKIE_PARAM_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
class ResourceTokenExchanger {

    private final BeanValidator validator;
    private final UserService userService;
    private final ObjectFactory<ProblemBuilder> problemBuilder;
    private final AuthTokenCreator authTokenCreator;

    public Either<Problem, AuthTokensResource> exchange(
            UUID userId,
            ResourceTokenExchangeRequest exchangeRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Exchanging resource token for user {}: {}", userId, exchangeRequest);
        return validator.validateAndGet(exchangeRequest, () ->
                exchangeValidated(userId, exchangeRequest, request, response));
    }

    private Either<Problem, AuthTokensResource> exchangeValidated(
            UUID userId,
            ResourceTokenExchangeRequest exchangeRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (!userService.isSelfOrAdmin(userId))
            return Either.left(notFound(userId));

        return CommonUtils
                .fetchCookie(request, CLIENT_ID_COOKIE_PARAM_NAME)
                .map(cookie -> Either.<Problem, String>right(cookie.getValue()))
                .orElseGet(() -> cookieNotFound(userId, exchangeRequest))
                .filter((String cookieValue) -> cookieMatchesRequest(cookieValue, exchangeRequest, userId))
                .flatMap(myClientId -> exchangeResourceToken(userId, exchangeRequest.resourceTokenValidForMillis(), request, response));
    }

    private Optional<Problem> cookieMatchesRequest(String cookieValue, ResourceTokenExchangeRequest exchangeRequest, UUID userId) {
        if (exchangeRequest.myClientId().equals(cookieValue))
            return Optional.empty();
        log.warn("{} cookie {} different from the given {} for user {}",
                CLIENT_ID_COOKIE_PARAM_NAME, cookieValue, exchangeRequest, userId);
        return Optional.of(notFound(userId));
    }

    private Either<Problem, String> cookieNotFound(UUID userId, ResourceTokenExchangeRequest exchangeRequest) {
        log.warn("{} cookie not found when trying to exchange resource token for user {} for {}",
                CLIENT_ID_COOKIE_PARAM_NAME, userId, exchangeRequest);
        return Either.left(notFound(userId));
    }

    private Problem notFound(UUID userId) {
        return problemBuilder.getObject().build(ProblemType.NOT_FOUND, "User %s not found".formatted(userId));
    }

    private Either<Problem, AuthTokensResource> exchangeResourceToken(
            UUID userId,
            Long resourceTokenValidForMillis,
            HttpServletRequest request,
            HttpServletResponse response) {

        deleteCookies(request, response, CLIENT_ID_COOKIE_PARAM_NAME);
        return authTokenCreator.create(userId, resourceTokenValidForMillis);
    }
}
