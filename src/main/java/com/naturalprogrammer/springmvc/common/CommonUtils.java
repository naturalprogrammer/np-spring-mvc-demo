package com.naturalprogrammer.springmvc.common;

import com.naturalprogrammer.springmvc.common.error.Problem;
import io.jbock.util.Either;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class CommonUtils {

    public static final String UNKNOWN = "UNKNOWN";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String CONTENT_TYPE_PREFIX = "application/vnd.com.naturalprogrammer.";

    public Optional<UUID> getUserId() {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return Optional.empty();

        var principal = auth.getPrincipal();
        return switch (principal) {
            case JwtAuthenticationToken token -> Optional.of(UUID.fromString(token.getName()));
            default -> Optional.empty();
        };
    }

    public static <T> ResponseEntity<?> toResponse(Either<Problem, T> either, Function<T, ResponseEntity<T>> success) {
        return either.fold(Problem::toResponse, success);
    }
}
