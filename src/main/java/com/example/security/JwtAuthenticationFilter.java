package com.example.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private final String AUTHORIZATION_HEADER = "Authorization";
	
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = this.getTokenFromRequest(request);
			
			//check token exists and token is valid
			//extract userId from the token if and only if the given token 
			//is really signature by this application and not expired
			if(StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
				Long userId = jwtProvider.extractUserLongIdFromToken(jwt);
				UserDetails userDetails = customUserDetailsService.loadUserById(userId);
				//in here, we will create authentication object create new UsernamePasswordAutheticationToken.
				//this "Authentication" object will be later passed into AuthenticationManager to be authenticated by
				//DaoAuthenticationProvider in this system.
				UsernamePasswordAuthenticationToken authentication = 
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}catch(Exception ex) {
			//any exception from hastText, validatedToken will throw the error message ???
			logger.error("Could not set user authetication in security context beacuse the failed authentication", ex);
		}
		filterChain.doFilter(request, response);
	}
	
	private String getTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		//check bearerToken is not null and starts with "Bearer "
		if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			System.out.println("BEARER TOKEN: " + bearerToken.substring("Bearer ".length()));
			return bearerToken.substring("Bearer ".length());
		}
		return null;
	}

}
