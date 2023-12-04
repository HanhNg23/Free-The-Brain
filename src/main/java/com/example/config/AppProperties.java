package com.example.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
	

	private final Auth auth = new Auth();
	private final OAuth2 oauth2 = new OAuth2();

	public static class Auth { //for authentication with Basic Authentication
		private String tokenSecret;
		private long accessTokenExpirationMsec;
		private long refreshTokenExpirationMsec;

		public String getTokenSecret() {
			return tokenSecret;
		}

		public void setTokenSecret(String tokenSecret) {
			this.tokenSecret = tokenSecret;
		}

		public long getAccessTokenExpirationMsec() {
			return accessTokenExpirationMsec;
		}

		public void setAccessTokenExpirationMsec(long accessTokenExpirationMsec) {
			this.accessTokenExpirationMsec = accessTokenExpirationMsec;
		}

		public long getRefreshTokenExpirationMsec() {
			return refreshTokenExpirationMsec;
		}

		public void setRefreshTokenExpirationMsec(long refreshTokenExpirationMsec) {
			this.refreshTokenExpirationMsec = refreshTokenExpirationMsec;
		}
	}

	public static final class OAuth2 { //for authentication with OAuth2 Provider
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
