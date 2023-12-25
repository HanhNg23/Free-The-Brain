package com.freethebrain.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank
    private String accoutName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
    
    public SignupRequest (String accountName, String email, String password) {
    	super();
    	this.accoutName = accountName;
    	this.email = email;
    	this.password = password;
    }

}
