package com.naturalprogrammer.springmvc.config.security;


import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.AUTH;
import static com.naturalprogrammer.springmvc.common.jwt.JwtPurpose.PURPOSE;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.SCOPE;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        var purpose = jwt.getClaim(PURPOSE);
        if (notEqual(purpose, AUTH.name())) {
            throw new BadCredentialsException(
                    "Authentication failed because the purpose of Jwt is not %s but %s: %s".formatted(
                            AUTH, purpose, jwt
                    ));
        }

        var userIdStr = jwt.getSubject();
        var userId = UUID.fromString(userIdStr);

        var user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User {} not found while logging in with JWT {}", userIdStr, jwt);
            return new AccountExpiredException("User %s not found".formatted(userIdStr));
        });

        var obsoleteToken = user.getTokensValidFrom().isAfter(requireNonNull(jwt.getIssuedAt()));
        if (obsoleteToken) {
            log.warn("Obsolete token {} used for user {}", jwt, user);
            throw new CredentialsExpiredException("Obsolete token used for user %s".formatted(userIdStr));
        }

        var authorities = getAuthorities(user);

        var scope = jwt.getClaimAsString(SCOPE); // e.g. "openid email profile"
        if (scope != null)
            authorities.addAll(getScopes(scope));

        return new JwtAuthenticationToken(jwt, authorities, userIdStr);
    }

    public Collection<SimpleGrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Collection<SimpleGrantedAuthority> getScopes(String scopes) {
        return Arrays.stream(scopes.split("\\s+"))
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .toList();
    }

}
