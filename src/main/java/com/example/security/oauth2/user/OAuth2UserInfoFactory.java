package com.example.security.oauth2.user;


import java.util.Map;

import com.example.exception.OAuth2AuthenticationProcessingException;
import com.example.model.AuthProvider;

public class OAuth2UserInfoFactory {

	public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, 
		Map<String, Object> attributes) throws OAuth2AuthenticationProcessingException {
		
		if(registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.name().toString())) {
			return new GoogleOAuth2User(attributes);
		}else {
			throw new OAuth2AuthenticationProcessingException(
					"Sorry! Login with " + registrationId + " is not supported yet.");
		}
		
	}
	
	
}
