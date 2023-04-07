/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.springmvc.config.sociallogin;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.config.MyProperties;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenCreator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import static com.naturalprogrammer.springmvc.common.CommonUtils.deleteCookies;
import static com.naturalprogrammer.springmvc.config.sociallogin.HttpCookieOAuth2AuthorizationRequestRepository.AUTHORIZATION_REQUEST_COOKIE_NAME;
import static com.naturalprogrammer.springmvc.config.sociallogin.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_COOKIE_PARAM_NAME;


/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 *
 * @author Sanjay Patel
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthTokenCreator authTokenCreator;
    private final MyProperties properties;

    @Override
    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response) {

        var userId = getCurrentUserId();
        String refreshingResourceToken = authTokenCreator.createClientSpecificResourceToken(userId);

        String targetUrl = CommonUtils
                .fetchCookie(request, REDIRECT_URI_COOKIE_PARAM_NAME)
                .map(Cookie::getValue)
                .orElse(properties.oauth2AuthenticationSuccessUrl());

        deleteCookies(request, response,
                AUTHORIZATION_REQUEST_COOKIE_NAME,
                REDIRECT_URI_COOKIE_PARAM_NAME);

        return targetUrl.formatted(userId, refreshingResourceToken);
    }

    private String getCurrentUserId() {
        var auth = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var socialUser = (SocialUser) auth.getPrincipal();
        return socialUser.getUserId().toString();
    }
}
