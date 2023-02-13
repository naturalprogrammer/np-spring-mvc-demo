package com.naturalprogrammer.springmvc.config.sociallogin;

import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleLoginProvider implements SocialLoginProvider {

    @Override
    public String getRegistrationId() {
        return "google";
    }

    @Override
    public boolean isEmailVerified(Map<String, Object> oauth2Attributes) {

        Object verified = oauth2Attributes.get(StandardClaimNames.EMAIL_VERIFIED);
        if (verified == null)
            verified = oauth2Attributes.get("verified");

        return (boolean) verified;
    }
}
