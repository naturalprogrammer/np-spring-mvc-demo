package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.features.login.ResourceTokenResource;
import com.naturalprogrammer.springmvc.user.features.signup.SignupRequest;
import com.naturalprogrammer.springmvc.user.features.verification.VerificationMailSender;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CommonUtils commonUtils;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final UserRepository userRepository;
    private final VerificationMailSender verificationMailSender;

    public UserResource toResponse(User user) {
        return toResponse(user, null);
    }

    public UserResource toResponse(User user, ResourceTokenResource token) {
        return new UserResource(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getLocale().toLanguageTag(),
                user.getRoles(),
                token
        );
    }

    public boolean isSelfOrAdmin(UUID userId) {

        return commonUtils.getUserId()
                .map(currentUserId -> isSelf(currentUserId, userId) || isAdmin(userId))
                .orElse(false);
    }

    public boolean isSelfOrAdmin(User user) {

        return commonUtils.getUserId()
                .map(currentUserId -> isSelf(currentUserId, user.getId()) || isAdmin(user))
                .orElse(false);
    }

    private boolean isSelf(UUID currentUserId, UUID userId) {
        if (notEqual(currentUserId, userId)) {
            log.info("Current user {} is not same as user {}", currentUserId, userId);
            return false;
        }
        return true;
    }

    private boolean isAdmin(UUID userId) {

        return userRepository
                .findById(userId)
                .map(this::isAdmin)
                .orElse(false);
    }

    private boolean isAdmin(User currentUser) {

        if (currentUser.isAdmin())
            return true;

        log.warn("Current user {} is not an admin", currentUser);
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(SignupRequest request, Locale locale, Role... roles) {

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setRoles(Set.of(roles));
        user.setLocale(locale);
        user.setTokensValidFrom(clock.instant().truncatedTo(ChronoUnit.SECONDS));
        var savedUser = userRepository.save(user);
        if (user.hasRoles(Role.UNVERIFIED))
            verificationMailSender.send(savedUser);
        return savedUser;
    }

}
