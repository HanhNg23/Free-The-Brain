package com.freethebrain.exception;

import javax.naming.AuthenticationException;

public class OAuth2AuthenticationProcessingException extends AuthenticationException {

	public OAuth2AuthenticationProcessingException(String message) {
		super(message);
	}
}
