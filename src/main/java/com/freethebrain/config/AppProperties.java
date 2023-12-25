package com.freethebrain.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Auth auth = new Auth();
	private final OAuth2 oauth2 = new OAuth2();

	@Setter
	@Getter
	public static class Auth { // for authentication with Basic Authentication Provider
		private String tokenSecret;
		private long accessTokenExpirationMsec;
		private long refreshTokenExpirationMsec;
		private String accessTokenName;
		private String refreshTokenName;
	}

	public static final class OAuth2 { // for authentication with OAuth2 Provider
		private List<String> authorizedRedirectUris = new ArrayList<>();

		public List<String> getAuthorizedRedirectUris() {
			return authorizedRedirectUris;
		}

		public OAuth2 setAuthorizedRedirectUris(List<String> authorizedRedirectUriList) {
			this.authorizedRedirectUris = authorizedRedirectUriList;
			return this; // retrun this OAuth2 child class
		}
	}

	public Auth getAuth() {
		return auth;
	}

	public OAuth2 getOauth2() {
		return oauth2;
	}

}
