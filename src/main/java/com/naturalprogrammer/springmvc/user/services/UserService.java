package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
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
    private final UserRepository userRepository;

    public UserResource toResponse(User user, String token) {
        return new UserResource(
                user.getIdStr(),
                user.getEmail(),
                user.getDisplayName(),
                user.getLocale().toLanguageTag(),
                user.getRoles(),
                token
        );
    }

    public boolean isSelfOrAdmin(UUID userId) {

        return commonUtils.getUserId()
                .map(currentUserId -> isSelf(currentUserId, userId) || isAdmin(currentUserId))
                .orElse(false);
    }

    private boolean isSelf(UUID currentUserId, UUID userId) {
        if (notEqual(currentUserId, userId)) {
            log.info("Current user {} is not same as user {}", currentUserId, userId);
            return false;
        }
        return true;
    }

    private boolean isAdmin(UUID currentUserId) {

        var currentUser = userRepository.findById(currentUserId).orElseThrow();
        if (currentUser.hasRoles(Role.ADMIN, Role.VERIFIED))
            return true;

        log.warn("Current user {} is not an admin", currentUser);
        return false;
    }
}
