package com.naturalprogrammer.springmvc.config.sociallogin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

import static com.naturalprogrammer.springmvc.config.sociallogin.HttpCookieOAuth2AuthorizationRequestRepository.AUTHORIZATION_REQUEST_COOKIE_NAME;
import static com.naturalprogrammer.springmvc.config.sociallogin.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_COOKIE_PARAM_NAME;

public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception
    ) throws ServletException, IOException {

        HttpCookieOAuth2AuthorizationRequestRepository.deleteCookies(request, response,
                AUTHORIZATION_REQUEST_COOKIE_NAME,
                REDIRECT_URI_COOKIE_PARAM_NAME
        );

        super.onAuthenticationFailure(request, response, exception);
    }
}
