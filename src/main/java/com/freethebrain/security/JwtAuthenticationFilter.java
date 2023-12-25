package com.freethebrain.security;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.freethebrain.config.AppProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String AUTHORIZATION_HEADER = "Authorization";
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private AppProperties appProperties;
    
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	    throws ServletException, IOException {
	    System.out.println("**** BEARER TOKEN: " + request.getHeader(AUTHORIZATION_HEADER));

		try {
			String jwt = this.getTokenFromRequest(request);
			// check token exists and token is valid
			// extract userId from the token if and only if the given token
			// is really signature by this application and not expired
			if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
				String userId = jwtProvider.extractUserIdFromToken(jwt);
				UserDetails userDetails = customUserDetailsService.loadUserById(userId);
				System.out.println("****************** UserDetails : " + userDetails.getUsername() + " - " + userDetails.getPassword() + " - " + userDetails.getAuthorities());
				// in here, we will create authentication object create new
				// UsernamePasswordAutheticationToken.
				// this "Authentication" object will be later passed into AuthenticationManager
				// for authentication by using
				// DaoAuthenticationProvider which has configured in the SecurityConfig class.
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(this.authenticationDetailsSource.buildDetails(request)); //without setDetails the Authentication obj will be null
				System.out.println("SECURITY CONTEXT HOLDER GET CONTEXT " 
				+ "- Details: " + authentication.getDetails() 
				+ "\n- Principle: " + authentication.getPrincipal().toString()
				+ "\n- Credentials: " + authentication.getCredentials()
				+ "\n- Authorities: " + authentication.getAuthorities()
				+ "\n- Class: " + authentication.getClass());
				System.out.println("SECURITY CONTEXT HOLDER GET CONTEXT BEFORE " + SecurityContextHolder.getContext().toString());

			//After verified the current user is valid them going to set the current Authentication in the Security Context to get out of null
				//SecurityContextHolder.getContext().setAuthentication(authentication);
				
				//OR create new SecurityContext for SecurityConctextHolder
				
				SecurityContext securityContext = SecurityContextHolder.createEmptyContext(); //not use it will deleted already inforamtion of security context holder
				securityContext.setAuthentication(authentication); //not use
				SecurityContextHolder.setContext(securityContext);

				System.out.println("SECURITY CONTEXT HOLDER GET CONTEXT AFTER" + SecurityContextHolder.getContext().toString());
			}
		} catch (Exception ex) {
			logger.error("Could not set user authetication in security context beacuse the failed authentication", ex);
		}
		filterChain.doFilter(request, response);
	}

	private String getTokenFromRequest(HttpServletRequest request) {

		String bearerToken;
		String accessToken;
		String refreshToken;

		bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		accessToken = Arrays.stream(request.getCookies())
				.filter(cookie -> cookie.getName().equals(appProperties.getAuth().getAccessTokenName())).findAny()
				.map(Cookie::getValue).orElse(null);
		refreshToken = Arrays.stream(request.getCookies())
				.filter(cookie -> cookie.getName().equals(appProperties.getAuth().getRefreshTokenName())).findAny()
				.map(Cookie::getValue).orElse(null);

		System.out.println("AUTHORIZATION HEADER: " + bearerToken);
		System.out.println("ACCESS TOKEN: " + accessToken);
		System.out.println("REFRESH TOKEN: " + refreshToken);

		// check bearerToken is not null and starts with "Bearer "
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring("Bearer ".length());
		} else if (StringUtils.hasText(accessToken)) {
			return accessToken;
		}
		return null;
	}

}
