package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.security.CustomUserDetailsService;
import com.example.security.JwtAuthenticationFilter;
import com.example.security.RestAuthenticationEntryPoint;
import com.example.security.oauth2.CustomOAuth2UserService;
import com.example.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.example.security.oauth2.OAuth2AuthenticationSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

	@Autowired
	private OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

	@Autowired
	private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Autowired
	public JwtAuthenticationFilter jwtAuthenticationFilter;

	/*
	 * By default, Spring OAuth2 uses
	 * HttpSessionOAuth2AuthorizationRequestRepository to save the authorization
	 * request. >< But, since our service is stateless, we can't save it in the
	 * session. ==> We'll save the request in a Base64 encoded cookie instead.
	 */

	@Bean
	public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
		return new HttpCookieOAuth2AuthorizationRequestRepository();
	}

	@Bean(BeanIds.AUTHENTICATION_MANAGER)
	// This object will processes an Authentication request
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.customUserDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	@Bean
	private PasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}
	
	
	protected SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
		
		http
			.cors(withDefaults())
			.sessionManagement(sm -> sm
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					)//SprSe never create an HttpSession and never use it to obtain the SecurityContext
			.csrf(c -> c.disable()) // do not use csrf
			.formLogin(f -> f.disable()) //do not use default formlogin
			.httpBasic(h -> h.disable())
			.exceptionHandling(ex -> ex
					.authenticationEntryPoint(new RestAuthenticationEntryPoint())					
					)
			.authorizeHttpRequests(au -> au
					.requestMatchers("/",
							"/error",
							"/favion.ico",
							"/**/*.png",
							"/**/*.gif",
							"/**/*.svg",
							"/**/*.jpg",
							"/**/*.html",
							"/**/*.css",
							"/**/*.js"						
							).permitAll()
					.anyRequest().authenticated()
					)
			.oauth2Login(oa -> oa
					.authorizationEndpoint(auenp -> auenp
							.baseUri("/oauth2/authorize") //this baseUri also the default
							//Sets the repository used for storing OAuth2AuthorizationRequest's.
							.authorizationRequestRepository(cookieAuthorizationRequestRepository())
							)
					.redirectionEndpoint(re -> re
							.baseUri("/oauth2/redirect/*")
							)
					.userInfoEndpoint(user -> user
							.userService(customOAuth2UserService)
							)
					.successHandler(oauth2AuthenticationSuccessHandler)
					.failureHandler(oauth2AuthenticationFailureHandler)

					)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			;
		
		return http.build();
	}
	
	

}
