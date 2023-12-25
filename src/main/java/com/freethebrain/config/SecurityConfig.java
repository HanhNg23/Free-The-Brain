package com.freethebrain.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.freethebrain.security.CustomUserDetailsService;
import com.freethebrain.security.JwtAuthenticationFilter;
import com.freethebrain.security.oauth2.CustomOAuth2UserService;
import com.freethebrain.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.freethebrain.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.freethebrain.security.oauth2.OAuth2AuthenticationSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;
/**
OAuth2AuthorizationCodeGrantFilter 
OAuth2AuthorizationRequestRedirectFilter 
OAuth2AuthorizationCodeAuthenticationToken 
DefaultServerOAuth2AuthorizationRequestResolver
InMemoryOAuth2AuthorizedClientService
OAuth2UserService 
DefaultOAuth2UserService 
**/
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
    
    @Bean
    public PasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}

    @Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.customUserDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}
    
	//Wake up to build AuthenticationManager object with 
    //the previous configurations in this class PasswordEncoder, AuthenticationProvider
	@Bean(BeanIds.AUTHENTICATION_MANAGER)
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	// OR  
	// Register new authentication provider above for configuration
	// authentication provider use for authentication
	public AuthenticationManagerBuilder config (AuthenticationManagerBuilder auth) {
		return auth.authenticationProvider(authenticationProvider());
		 
	}
	@Bean
	protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(withDefaults())
				// create an HttpSession and it will never use itto obtain the SecurityContext
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			//.csrf(withDefaults()) 
			.formLogin(withDefaults())
			.authorizeHttpRequests(au -> au
					 .requestMatchers("/small/home/**","/home/**", "/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/login/**", "/error/**").permitAll()
					.anyRequest().authenticated())
//			.exceptionHandling(ex -> ex.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
			.oauth2Login(oa -> oa
						// Used by the client to obtain authorization from the resource owner through
						// user-agent redirection.
						// The base uri default is /oauth2/authorization/plush registrationId
					.authorizationEndpoint(endpont -> endpont.baseUri("/oauth2/authorize")
								// Sets the repository used for storing OAuth2AuthorizationRequest's.
							.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
						// Used by the authorization server to return responses that contain
						// authorization credentials to the client through the resource owner user-agent
						// The default base uri end point /login/oauth2/code/*
					.redirectionEndpoint(re -> re.baseUri("/oauth2/callback/*"))
						// The client use to exchange an authorization grant for an access token,
						// typically with client authentication
					.tokenEndpoint(withDefaults())
						// An OAuth 2.0 Protected Resource that returns claims about the authenticated
						// end-use
					.userInfoEndpoint(user -> user.userService(customOAuth2UserService))
					.successHandler(oauth2AuthenticationSuccessHandler)
					.failureHandler(oauth2AuthenticationFailureHandler)
					)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

}
