package com.naturalprogrammer.springmvc.config.sociallogin;

import java.util.Map;

public interface SocialLoginProvider {

    String getRegistrationId();

    boolean isEmailVerified(Map<String, Object> oauth2Attributes);
}
