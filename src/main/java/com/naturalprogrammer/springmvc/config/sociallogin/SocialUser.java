package com.naturalprogrammer.springmvc.config.sociallogin;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Getter
public class SocialUser implements OidcUser {

    private final UUID userId;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String name;
    private final Map<String, Object> claims;
    private final OidcUserInfo userInfo;
    private final OidcIdToken idToken;

    public SocialUser(UUID userId, OAuth2User oauth2User) {
        this.userId = userId;
        attributes = oauth2User.getAttributes();
        authorities = oauth2User.getAuthorities();
        name = oauth2User.getName();
        claims = null;
        userInfo = null;
        idToken = null;
    }

    public SocialUser(UUID userId, OidcUser oidcUser) {
        this.userId = userId;
        attributes = oidcUser.getAttributes();
        authorities = oidcUser.getAuthorities();
        name = oidcUser.getName();
        claims = oidcUser.getClaims();
        userInfo = oidcUser.getUserInfo();
        idToken = oidcUser.getIdToken();
    }
}
