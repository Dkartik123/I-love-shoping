package com.iloveshopping.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown during OAuth2 authentication processing.
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {

    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }
}
