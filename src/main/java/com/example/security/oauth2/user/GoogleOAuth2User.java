package com.example.security.oauth2.user;

import java.util.Map;

public class GoogleOAuth2User extends OAuth2UserInfo {

	public GoogleOAuth2User(Map<String, Object> attributes) {
		super(attributes);
	}

	@Override
	public String getId() {
		return (String) attributes.get("sub"); //using attributes property which locate in the OAuth2UserInfo Class
	}

	@Override
	public String getAccountName() {
		
		return (String) attributes.get("name");
	}

	@Override
	public String getEmail() {
		return (String) attributes.get("email");
	}

	@Override
	public String getImageurl() {
		return (String) attributes.get("picture");
	}

	
}
