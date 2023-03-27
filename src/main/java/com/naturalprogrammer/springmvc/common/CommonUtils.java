package com.naturalprogrammer.springmvc.common;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.config.sociallogin.SocialUser;
import io.jbock.util.Either;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class CommonUtils {

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String CONTENT_TYPE_PREFIX = "application/vnd.com.naturalprogrammer.";

    public Optional<UUID> getUserId() {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        return switch (auth) {
            case JwtAuthenticationToken jwtAuth -> Optional.of(UUID.fromString(jwtAuth.getName()));
            case OAuth2AuthenticationToken oAuth2AuthenticationToken -> getUserId(oAuth2AuthenticationToken);
            default -> Optional.empty();
        };
    }

    private Optional<UUID> getUserId(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        var socialUser = (SocialUser) oAuth2AuthenticationToken.getPrincipal();
        return Optional.of(socialUser.getUserId());
    }

    public static <T> ResponseEntity<?> toResponse(Either<Problem, T> either, Function<T, ResponseEntity<T>> success) {
        return either.fold(Problem::toResponse, success);
    }

    public static Optional<Cookie> fetchCookie(HttpServletRequest request, String name) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(name))
                    return Optional.of(cookie);

        return Optional.empty();
    }

    public static void deleteCookies(HttpServletRequest request, HttpServletResponse response, String... cookiesToDelete) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null)
            for (Cookie cookie : cookies)
                if (ArrayUtils.contains(cookiesToDelete, cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
    }

    /**
     * Serializes an object
     */
    public static String serialize(Serializable obj) {

        return Base64.getUrlEncoder().encodeToString(
                SerializationUtils.serialize(obj));
    }

    /**
     * Deserializes an object
     */
    public static <T> T deserialize(String serializedObj) {

        return SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(serializedObj));
    }

}
