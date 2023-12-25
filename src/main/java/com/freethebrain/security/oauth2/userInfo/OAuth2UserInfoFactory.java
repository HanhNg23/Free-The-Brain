package com.freethebrain.security.oauth2.userInfo;

import java.util.Map;
import com.freethebrain.exception.OAuth2AuthenticationProcessingException;
import com.freethebrain.model.AuthProvider;
import com.freethebrain.security.oauth2.userInfo.GoogleOAuth2UserInfo;
import com.freethebrain.security.oauth2.userInfo.OAuth2UserInfo;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes)
	    throws OAuth2AuthenticationProcessingException {

	if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.name())) {
	    return new GoogleOAuth2UserInfo(attributes);
	} else {
	    throw new OAuth2AuthenticationProcessingException(
		    "Sorry! Login with " + registrationId + " is not supported yet.");
	}

    }

}
