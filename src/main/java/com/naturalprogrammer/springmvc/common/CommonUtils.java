package com.naturalprogrammer.springmvc.common;

import com.naturalprogrammer.springmvc.common.error.Problem;
import io.jbock.util.Either;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

@Component
public class CommonUtils {

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String CONTENT_TYPE_PREFIX = "application/vnd.com.naturalprogrammer.";

    public Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
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
