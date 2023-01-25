package com.naturalprogrammer.springmvc.config;


import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        var userIdStr = jwt.getClaimAsString("sub");
        var userId = UUID.fromString(userIdStr);

        var authorities = userRepository.findById(userId)
                .map(User::getAuthorities)
                .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(userIdStr)));

        var scope = jwt.getClaimAsString("scope"); // e.g. "openid email profile"
        if (scope != null) {
            authorities = new ArrayList<>(authorities);
            authorities.addAll(getScopeList(scope));
        }

        return new JwtAuthenticationToken(jwt, authorities, userIdStr);

    }

    private Collection<SimpleGrantedAuthority> getScopeList(String scopes) {
        return Arrays.stream(StringUtils.delimitedListToStringArray(scopes, " "))
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .toList();
    }

}
