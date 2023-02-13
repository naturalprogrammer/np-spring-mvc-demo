package com.naturalprogrammer.springmvc.config.sociallogin;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FacebookLoginProvider implements SocialLoginProvider {

    @Override
    public String getRegistrationId() {
        return "facebook";
    }

    @Override
    public boolean isEmailVerified(Map<String, Object> oauth2Attributes) {
        // Facebook no more returns verified
        // https://developers.facebook.com/docs/graph-api/reference/user
        return false;
    }
}
