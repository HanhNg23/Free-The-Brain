package com.freethebrain.security.oauth2;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.freethebrain.util.CookieUtils;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 *<p>
 * The OAuth2 protocol using "state" parameter to prevent CSRF attacks
 * --> During authentication, the application sends this parameter in the AUTHORIZATION REQUEST 
 * and the OAuth2 Provider returns this parameter unchanged in the OAuth2 CALLBACK
 * ---> The application compares the value of the "state" parameter returned from OAuth2 provider
 * with the initial value had send ---> If do not match --> access denied.
 * <p>
 * ==> By this way, the application can prevent the CSRF attacks
 * <p>
 * --------------------------------------------------------------
 * <p>
 * To achieve this flow, this application will store the "state" parameter as well as 
 * the "redirect_uri" in a short-lived cookie. 
 * ---> So that we can later compare it with the "state" parameter returned from the OAuth2 Provider in callback uri(redirect-uri)
 * <p>
 * --------------------------------------------------------------
 * <p>
 * This class replaces for default implementation of {@link AuthorizationRequestRepository} is
 * {@link HttpSessionOAuth2AuthorizationRequestRepository}
 * <p>
 * Used by filers 
 * <li>{@link OAuth2AuthorizationRequestRedirectFilter} when prepare sending authorization request to Authorization Server
 * <li>{@link OAuth2AuthorizationCodeGrantFilter} when the process of getting Authorization
 *  grant done and callback to client via the redirect-uri
 **/
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
		implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"; //uri that user want to access protected resource - differ redirect-uri in call back
	private static final int cookieExpireSeconds = 180;

	// We have customize to save the OAuth2AuthorizationRequest before sending the
	// request to AuthorizationServer in cookie safe to later compare the uri and its state in
	// call back and uri in pre-saved in cookie --> help to avoid csrf
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		Assert.notNull(request, "request cannot be null ");
		// get the authorization request from cookie
		OAuth2AuthorizationRequest authorizationRequest = CookieUtils
				.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
				.map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class)).orElse(null);
		// compare the state from saved request in cookie "oauth2_auth_request" and state from the request in rediect-uri
		// the "state" param has been generated by implentation of OAuth2AuthorizationRequestResolver is DefaultServerOAuth2AuthorizationRequestResolver
		String stateParameter = request.getParameter(OAuth2ParameterNames.STATE);
		return (authorizationRequest != null && stateParameter.equals(authorizationRequest.getState()))
				? authorizationRequest : null;
	}

	// Save the Authorization Request in cookie before sendRedirect
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
			HttpServletResponse response) {
		if (authorizationRequest == null) {
			this.removeAuthorizationRequest(request, response);
			return;
		}
		System.out.println(getOAuth2AuthorizationRequestToString(authorizationRequest));
		System.out.println("REDIRECT_URI_PARAM : " + request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME));
		// Check state exists in AuthorizationRequest
		String state = authorizationRequest.getState();
		Assert.hasText(state, "authorizationRequest.state cannot be empty");
		
		CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, 
									    CookieUtils.serialize(authorizationRequest), 
				                        cookieExpireSeconds);
		
		String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		
		if (StringUtils.isNotBlank(redirectUriAfterLogin))
			CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
		return this.loadAuthorizationRequest(request);
//		Assert.notNull(request, "request cannot be null");
//		Assert.notNull(response, "response cannot be null");
//		
//		OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
//		if(authorizationRequest != null) {
//			this.removeAuthorizationRequest(request, response);
//		}
//		return authorizationRequest;
	}

	public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
		CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
	}
	
	public String getOAuth2AuthorizationRequestToString(OAuth2AuthorizationRequest authorizationRequest) {
		return "OAUTH2_AUTHORIZATION_REQUEST : " + 
				"\n - AuthorizationRequestUri : " + authorizationRequest.getAuthorizationRequestUri() + 
				"\n - AuthorizationUri : " + authorizationRequest.getAuthorizationUri() + 
				"\n - ClientId : " + authorizationRequest.getClientId() + 
				"\n - RedirectUri : " + authorizationRequest.getRedirectUri() + 
				"\n - State : " + authorizationRequest.getState() + 
				"\n - Map - AdditionalParameters : " + authorizationRequest.getAdditionalParameters().toString() + 
				"\n - Map - Attributes : " + authorizationRequest.getAttributes().toString() + 
				"\n - GrantType : " + authorizationRequest.getGrantType().toString() +
				"\n - ResponseType : " + authorizationRequest.getResponseType().toString() + 
				"\n - Scopes : " + authorizationRequest.getScopes().toString(); 
	}

}
