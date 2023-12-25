package com.freethebrain.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    public LoginRequest(String email, String password) {
    	super();
    	this.email = email;
    	this.password = password;
    }
}
