package com.freethebrain.payload;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class AuthResponse {
	
	private String accessToken;
	
	private String refreshToken;

	///AccessLevel None means not allow create setter for this tokenType but only allow for getter
	@Setter(value = AccessLevel.NONE) 
	private String tokenType = "Bearer";

	public AuthResponse(String accessToken, String refreshToken) {
		super();
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

}
