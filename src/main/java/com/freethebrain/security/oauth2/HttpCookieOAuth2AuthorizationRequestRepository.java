package com.freethebrain.security.oauth2;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.freethebrain.util.CookieUtils;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//httpcookie will be the place = repository for persisting authorization Request as well as
//OAuth2 authorization Request
/*The OAuth2 protocol using "state" parameter to prevent CSRF attacks.
 *--> During authentication, the application sends this parameter in the AUTHORIZATION REQUEST 
 *and the OAuth2 Provider returns this parameter unchanged in the OAuth2 CALLBACK
 *---> Now, application compares the value of the "state" parameter returned fr OAuth2 provider
 *	   with the initial value had send.
 *---> If do not match --> access denied
 *
 *NOW, my application need to should store the "state" parameter as well as the "redirect_uri" in a short-lived cookie. 
 *So that ---> we can later compare it with the "state" returned fr the OAuth2 Provider
 * */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int cookieExpireSeconds = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {

	return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
		.map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class)) // it only map if
												  // getCookie presents
												  // value
		.orElse(null);
	// map is a function of Optional<T>
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
	    HttpServletResponse response) {
	if (authorizationRequest == null) {
	    CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
	    CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
	    return;
	}

	CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, // cookie name
		CookieUtils.serialize(authorizationRequest), // cookie value
		cookieExpireSeconds);// cookie expire time
	String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
	if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
	    CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
	}
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
	    HttpServletResponse response) {
	return this.loadAuthorizationRequest(request); // this will return null if the
						       // OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME
						       // does not exist in cookie list
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
	CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
	CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

}
