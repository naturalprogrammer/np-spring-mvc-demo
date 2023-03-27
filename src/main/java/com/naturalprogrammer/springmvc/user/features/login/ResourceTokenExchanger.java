package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.common.error.BeanValidator;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemComposer;
import com.naturalprogrammer.springmvc.common.error.ProblemType;
import io.jbock.util.Either;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProblemComposer problemComposer;
    private final AuthTokenCreator authTokenCreator;

    public Either<Problem, ResourceTokenResource> exchange(
            UUID userId,
            ResourceTokenExchangeRequest exchangeRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return validator.validateAndGet(exchangeRequest, ProblemType.INVALID_RESOURCE_TOKEN_EXCHANGE_REQUEST, () ->
                exchangeValidated(userId, exchangeRequest, request, response));
    }

    private Either<Problem, ResourceTokenResource> exchangeValidated(
            UUID userId,
            ResourceTokenExchangeRequest exchangeRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return CommonUtils
                .fetchCookie(request, CLIENT_ID_COOKIE_PARAM_NAME)
                .map(cookie -> Either.<Problem, String>right(cookie.getValue()))
                .orElseGet(() -> cookieNotFound(userId, exchangeRequest))
                .filter((String cookieValue) -> cookieMatchesRequest(cookieValue, exchangeRequest, userId))
                .flatMap(myClientId -> exchangeResourceToken(userId, exchangeRequest.resourceTokenValidForMillis(), request, response));
    }

    private Optional<Problem> cookieMatchesRequest(String cookieValue, ResourceTokenExchangeRequest exchangeRequest, UUID userId) {
        {
            if (exchangeRequest.myClientId().equals(cookieValue))
                return Optional.empty();
            log.warn("{} cookie {} different from the given {} for user {}",
                    CLIENT_ID_COOKIE_PARAM_NAME, cookieValue, exchangeRequest, userId);
            return Optional.of(notFound(userId));
        }
    }

    private Either<Problem, String> cookieNotFound(UUID userId, ResourceTokenExchangeRequest exchangeRequest) {
        log.warn("{} cookie not found when trying to exchange resource token for user {} for {}",
                CLIENT_ID_COOKIE_PARAM_NAME, userId, exchangeRequest);
        return Either.left(notFound(userId));
    }

    private Problem notFound(UUID userId) {
        return problemComposer.compose(ProblemType.NOT_FOUND, "User %s not found".formatted(userId));
    }

    private Either<Problem, ResourceTokenResource> exchangeResourceToken(
            UUID userId,
            Long resourceTokenValidForMillis,
            HttpServletRequest request,
            HttpServletResponse response) {

        deleteCookies(request, response, CLIENT_ID_COOKIE_PARAM_NAME);
        return authTokenCreator.create(userId, resourceTokenValidForMillis);
    }
}
