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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static com.naturalprogrammer.springmvc.common.CommonUtils.deleteCookies;

/**
 * Cookie based repository for storing Authorization requests
 */
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final int COOKIE_EXPIRY_SECONDS = 60;
	public static final String AUTHORIZATION_REQUEST_COOKIE_NAME = "my_oauth2_authorization_request";
	public static final String REDIRECT_URI_COOKIE_PARAM_NAME = "myRedirectUri";
	public static final String CLIENT_ID_COOKIE_PARAM_NAME = "myAttemptId";

	/**
	 * Load authorization request from cookie
	 */
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {

		return CommonUtils.fetchCookie(request, AUTHORIZATION_REQUEST_COOKIE_NAME)
				.map(this::deserialize)
				.orElse(null);
	}

	/**
	 * Save authorization request in cookie
	 */
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
										 HttpServletResponse response) {

		if (authorizationRequest == null) {
			deleteCookies(request, response,
					AUTHORIZATION_REQUEST_COOKIE_NAME,
					REDIRECT_URI_COOKIE_PARAM_NAME,
					CLIENT_ID_COOKIE_PARAM_NAME);
			return;
		}

		addCookie(response, AUTHORIZATION_REQUEST_COOKIE_NAME, CommonUtils.serialize(authorizationRequest));
		addParamCookie(request, response, REDIRECT_URI_COOKIE_PARAM_NAME);
		addParamCookie(request, response, CLIENT_ID_COOKIE_PARAM_NAME);
	}

	private void addParamCookie(HttpServletRequest request, HttpServletResponse response, String paramName) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotBlank(paramValue))
			addCookie(response, paramName, paramValue);
	}

	private void addCookie(HttpServletResponse response, String name, String value) {
		var cookie = newCookie(name, value);
		response.addCookie(cookie);
	}

	public static Cookie newCookie(String name, String value) {

		var cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(COOKIE_EXPIRY_SECONDS);
		return cookie;
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {

		OAuth2AuthorizationRequest originalRequest = loadAuthorizationRequest(request);
		deleteCookies(request, response, AUTHORIZATION_REQUEST_COOKIE_NAME);
		return originalRequest;
	}

	private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
		return CommonUtils.deserialize(cookie.getValue());
	}
}
