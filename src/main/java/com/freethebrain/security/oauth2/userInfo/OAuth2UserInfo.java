package com.freethebrain.security.oauth2.userInfo;

import java.util.Map;

import lombok.Data;
import lombok.ToString;

/*
 * Every OAuth2 provider returns a different JSON response when we fetch the authenticated user’s details.
 * Spring security parses the response in the form of a generic map of key-value pairs.
 * The following classes in this package are used to get the required details of the user
   from the generic map of key-value pairs .
 * */

public abstract class OAuth2UserInfo {

	protected Map<String, Object> attributes;

	public OAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
		// Note this list will contain all authenticated
		// user information in the key and value format
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public abstract String getId();

	public abstract String getAccountName();

	public abstract String getEmail();

	public abstract String getImageurl();
	
	public String toString() {
		return "Id: " + this.getId() + " - " 
		+ "\nAccountName: " + this.getAccountName() + " - "
		+ "\nEmail: " + this.getEmail() + " - "
		+ "\nImageurl: " + this.getImageurl() + " - ";
	}
}
