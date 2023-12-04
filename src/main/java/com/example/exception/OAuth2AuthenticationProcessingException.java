package com.example.exception;

import javax.naming.AuthenticationException;

public class OAuth2AuthenticationProcessingException extends AuthenticationException{

	public OAuth2AuthenticationProcessingException(String message) {
		super(message);
	}
	
//	public OAuth2AuthenticationProcessingException(String message,Throwable t) {
//		super(message, t);
//	}

}
