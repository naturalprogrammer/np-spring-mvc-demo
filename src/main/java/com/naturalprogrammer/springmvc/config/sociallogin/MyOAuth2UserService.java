package com.naturalprogrammer.springmvc.config.sociallogin;

import com.naturalprogrammer.springmvc.user.domain.Role;
import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.features.signup.SignupRequest;
import com.naturalprogrammer.springmvc.user.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MyOAuth2UserService extends DefaultOAuth2UserService {

    private final Map<String, SocialLoginProvider> loginProviders;
    private final UserService userService;

    public MyOAuth2UserService(
            List<SocialLoginProvider> loginProviders,
            UserService userService
    ) {
        this.loginProviders = loginProviders.stream().collect(Collectors.toMap(
                SocialLoginProvider::getRegistrationId,
                Function.identity()
        ));
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oauth2User = super.loadUser(userRequest);
        var user = getUser(oauth2User, userRequest.getClientRegistration().getRegistrationId());
        return new SocialUser(user.getId(), oauth2User);
    }

    /**
     * Builds the security principal from the given userReqest.
     * Registers the user if not already registered
     */
    public User getUser(OAuth2User oauth2User, String registrationId) {

        var attributes = oauth2User.getAttributes();
        var email = (String) attributes.get(StandardClaimNames.EMAIL);

        return userService
                .findByEmail(email)
                .map(user -> {
                    log.info("Got {} for {}", user, oauth2User);
                    return user;
                })
                .orElseGet(() -> {
                    var signupRequest = new SignupRequest(
                            email,
                            UUID.randomUUID().toString(),
                            email.substring(0, email.indexOf('@')),
                            null
                    );
                    var emailVerified = isEmailVerified(registrationId, attributes);
                    var role = emailVerified ? Role.VERIFIED : Role.UNVERIFIED;

                    var user = userService.createUser(signupRequest, Locale.ENGLISH, role);
                    log.info("Created {} for {}", user, oauth2User);
                    return user;
                });
    }

    private boolean isEmailVerified(String registrationId, Map<String, Object> attributes) {
        var provider = loginProviders.get(registrationId);
        return provider != null && provider.isEmailVerified(attributes);
    }

}
