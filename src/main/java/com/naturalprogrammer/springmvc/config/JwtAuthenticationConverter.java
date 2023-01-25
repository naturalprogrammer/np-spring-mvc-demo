package com.naturalprogrammer.springmvc.config;


import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        var userIdStr = jwt.getClaimAsString("sub");
        var userId = UUID.fromString(userIdStr);

        return userRepository.findById(userId)
                .map(MyUser::getAuthorities)
                .map(authorities -> new JwtAuthenticationToken(jwt, authorities, userIdStr))
                .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(userIdStr)));
    }

}
