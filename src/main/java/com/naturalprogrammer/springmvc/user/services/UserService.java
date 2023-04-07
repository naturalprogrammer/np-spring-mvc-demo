package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.common.CommonUtils;
import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.features.login.AuthTokensResource;
import com.naturalprogrammer.springmvc.user.features.signup.SignupRequest;
import com.naturalprogrammer.springmvc.user.features.verification.VerificationMailSender;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public UserResource toResponse(User user, AuthTokensResource token) {
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

        return commonUtils.getAuthentication()
                .map(authentication -> isSelf(authentication.getName(), userId) || isAdmin(authentication))
                .orElse(false);
    }

    private boolean isSelf(String currentUserId, UUID userId) {
        if (notEqual(currentUserId, userId.toString())) {
            log.info("Current user {} is not same as user {}", currentUserId, userId);
            return false;
        }
        return true;
    }

    private boolean isAdmin(Authentication authentication) {

        var admin = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet())
                .containsAll(Set.of(Role.VERIFIED.authority(), Role.ADMIN.authority()));

        if (!admin)
            log.warn("Current user {} is not an admin. Available authorities: {}",
                    authentication.getName(), authentication.getAuthorities());

        return admin;
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
