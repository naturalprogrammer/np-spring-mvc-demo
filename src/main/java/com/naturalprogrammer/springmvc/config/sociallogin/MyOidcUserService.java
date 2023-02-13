package com.naturalprogrammer.springmvc.config.sociallogin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyOidcUserService extends OidcUserService {

    private final MyOAuth2UserService oauth2UserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {

        OidcUser oidcUser = super.loadUser(userRequest);
        var user = oauth2UserService.getUser(oidcUser,
                userRequest.getClientRegistration().getRegistrationId());
        return new SocialUser(user.getId(), oidcUser);
    }

}
