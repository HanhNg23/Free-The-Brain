package com.freethebrain.security.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.freethebrain.util.CookieUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * In case of any error during OAuth2 authentication, Spring Security
 * invokes the onAuthenticationFailure() method of the OAuth2AuthenticationFailureHandler
 * that we have configured in SecurityConfig.
	--> It sends the user to the frontend client with an error message
	 added to the query string -
 */
@Component
public abstract class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
	    AuthenticationException exception) throws IOException, ServletException {
	String targetUrl = CookieUtils
		.getCookie(request, httpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
		.map(t -> t.getValue()) // or map(Cookie::getValue))
		.orElse(("/"));
	targetUrl = UriComponentsBuilder.fromUriString(targetUrl).queryParam("error", exception.getLocalizedMessage())
		.build().toUriString();

	httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);

	getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
