package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokenResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CommonUtils commonUtils;

    public UserResource toResponse(User user) {
        return toResponse(user, null);
    }

    public UserResource toResponse(User user, AuthTokenResource token) {
        return new UserResource(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getLocale().toLanguageTag(),
                user.getRoles(),
                token
        );
    }

    public boolean isSelfOrAdmin(User user) {

        return commonUtils.getUserId()
                .map(currentUserId -> isSelf(currentUserId, user) || isAdmin(user))
                .orElse(false);
    }

    private boolean isSelf(UUID currentUserId, User user) {
        if (notEqual(currentUserId, user.getId())) {
            log.info("Current user {} is not same as user {}", currentUserId, user);
            return false;
        }
        return true;
    }

    private boolean isAdmin(User currentUser) {

        if (currentUser.isAdmin())
            return true;

        log.warn("Current user {} is not an admin", currentUser);
        return false;
    }
}
