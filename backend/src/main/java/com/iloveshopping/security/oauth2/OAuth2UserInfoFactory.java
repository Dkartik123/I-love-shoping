package com.iloveshopping.security.oauth2;

import com.iloveshopping.exception.OAuth2AuthenticationProcessingException;

import java.util.Map;

/**
 * Factory for creating OAuth2UserInfo instances based on the provider.
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("facebook")) {
            return new FacebookOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Login with " + registrationId + " is not supported.");
        }
    }
}
