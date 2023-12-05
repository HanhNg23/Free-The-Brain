package com.freethebrain.payload;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
public class AuthResponse {
    private String accessToken;

    private String refreshToken;

    @Setter(value = AccessLevel.NONE)
    private String tokenType = "Bearer";

    public AuthResponse(String accessToken, String refreshToken) {
	super();
	this.accessToken = accessToken;
	this.refreshToken = refreshToken;
    }

}
