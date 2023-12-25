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
 * In this method, our purpose is performing some validation after user login successfully:
 *  CREATE a JWT authentication token 
 *  ++
 *  REDIRECT user to the "redirect_uri" specified by the client
 * with the JWT token added in the query string
 * The default configuration locate at SavedRequestAwareAuthenticationSuccessHandler class
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
	    Authentication authentication) throws IOException, ServletException {

	// Builds the target URL bases on arguments fields request, response, authentication
	String targetUrl = determineTargetUrl(request, response, authentication);

	if (response.isCommitted()) {
	    logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
	    return;
	}

		//	Removes temporary authentication-related data which may have been
		//	stored in thesession during the authentication process.
		clearAuthenticationAttributes(request);

		// Success authentication the redirect back to the resource that user request.
		//getRedirectStrategy().sendRedirect(request, response, targetUrl); 
		getRedirectStrategy().sendRedirect(request, response, "/small/home");
    }

    //REDIRECT user to the "redirect_uri" specified by the client
    //with the JWT token added in the query string
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

		return UriComponentsBuilder.fromUriString(targetUrl)
				.queryParam("accessToken", accessToken)//Append the given query parameter
				.queryParam("refreshToken", refreshTokn)
				.build()
				.toUriString();

	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

	private boolean isAuthorizedRedirectUri(String uri) {
		URI clientRedirectUri = URI.create(uri);
		return appProperties
				.getOauth2()
				.getAuthorizedRedirectUris()
				.stream()
				.anyMatch(authorizedRedirectUri -> {
			URI authorizedURI = URI.create(authorizedRedirectUri);
			if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
					&& authorizedURI.getPort() == clientRedirectUri.getPort()) {
				return true;
			}
			return false;
		});
	}

}
