package com.example.model;

public enum AuthProvider {

	LOCAL("local"),
	FACEBOOK("facebook"),
	GOOGLE("google"),
	GITHUB("github")
	;
	
	private String authProvider;
	
	private AuthProvider(String authProvider) {
		this.authProvider = authProvider;
	}
}
