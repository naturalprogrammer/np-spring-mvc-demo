package com.naturalprogrammer.springmvc.common;

import com.naturalprogrammer.springmvc.user.domain.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtils {

    public static final String UNKNOWN = "UNKNOWN";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String CONTENT_TYPE_PREFIX = "application/vnd.com.naturalprogrammer.";

    public Optional<User> getUser() {

        return Optional.empty();
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null)
//            return Optional.empty();
//
//        var principal = auth.getPrincipal();
//        if (principal)

    }
}
