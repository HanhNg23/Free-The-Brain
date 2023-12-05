package com.freethebrain.security.oauth2;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.freethebrain.config.AppProperties;
import com.freethebrain.exception.BadRequestException;
import com.freethebrain.security.JwtProvider;
import com.freethebrain.util.CookieUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * On successful authentication, Spring security invokes the onAthenticationSucess() 
 * of the OAuth2AuthenticationSuccesHandler which configured in SecurityConfig
 * 
 * In this method, our purpose is performing some validation after user login successfully, 
 * We will CREATE a JWT authentication token ++ REDIRECT user to the "redirect_uri" which
 * "specified by the client with the JWT token added in the query string" ??
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Autowired
    public OAuth2AuthenticationSuccessHandler(JwtProvider jwtProvider, AppProperties appProperties,
	    HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
	this.jwtProvider = jwtProvider;
	this.appProperties = appProperties;
	this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
	    Authentication authentication) throws IOException, ServletException {

	// Builds the target URL according to the logic fields request, response,
	// authentication
	String targetUrl = determineTargetUrl(request, response, authentication);

	if (response.isCommitted()) {
	    logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
	    return;
	}

	clearAuthenticationAttributes(request);

	getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
	    Authentication authentication) {
	Optional<String> redirectUri = CookieUtils
		.getCookie(request, httpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
		.map(Cookie::getValue);
	if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
	    throw new BadRequestException(
		    "Sorry! We've got an Unauthorized Redirect URI and can't" + " proceed with the authentication");
	}

	String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
	String accessToken = jwtProvider.createAccessToken(authentication);
	String refreshTokn = jwtProvider.createRefresfToken(authentication);

	return UriComponentsBuilder.fromUriString(targetUrl).queryParam("accessToken", accessToken)
		.queryParam("refreshTokn", refreshTokn).build().toUriString();

    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
	super.clearAuthenticationAttributes(request);
	httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
	URI clientRedirectUri = URI.create(uri);

	return appProperties.getOauth2().getAuthorizedRedirectUris().stream().anyMatch(authorizedRedirectUri -> {
	    URI authorizedURI = URI.create(authorizedRedirectUri);
	    if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
		    && authorizedURI.getPort() == clientRedirectUri.getPort()) {
		return true;
	    }
	    return false;
	});
    }

}
