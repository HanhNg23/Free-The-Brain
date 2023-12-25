package com.freethebrain.controller;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.freethebrain.exception.ResourceNotFoundException;
import com.freethebrain.model.User;
import com.freethebrain.payload.ApiResponse;
import com.freethebrain.repository.UserRepository;
import com.freethebrain.security.CurrentUser;
import com.freethebrain.security.UserPrincipal;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
	
	private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2AuthorizedClientService auth2AuthorizedClientService;
    
    @GetMapping("/user/me")
	public ApiResponse<?> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
		User user = userRepository.findById(userPrincipal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
		return new ApiResponse<User>(HttpStatus.OK, user);
	}
    
    @GetMapping("/user/details")
    //@Pre-Authorize
    public ResponseEntity<?> getUserInforInDetails(@CurrentUser UserPrincipal userPrincipal){
    	
    	SecurityContext securityContext = SecurityContextHolder.getContext();
    	OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) securityContext.getAuthentication();
    	System.out.println("PRINCIPAL NAME : " + oAuth2AuthenticationToken.getName());
		OAuth2AuthorizedClient auth2AuthorizedClient = auth2AuthorizedClientService.loadAuthorizedClient(
				oAuth2AuthenticationToken.getAuthorizedClientRegistrationId(), oAuth2AuthenticationToken.getName());
    	String accessToken = auth2AuthorizedClient.getAccessToken().getTokenValue();
    	String refreshToken = auth2AuthorizedClient.getRefreshToken().getTokenValue();
    	System.out.println("Registration Id: " + auth2AuthorizedClient.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri());
    	System.out.println("Username attributes: " + auth2AuthorizedClient.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
    	
    	String userInfoUri = auth2AuthorizedClient.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
    	String userNameAttributeName = auth2AuthorizedClient.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
    	if(!StringUtils.hasText(userInfoUri) || !StringUtils.hasText(userNameAttributeName)) {
    		throw new OAuth2AuthenticationException("User info endpoint cannot be empty");
    	}
    	
    	HttpHeaders headers = new HttpHeaders();
    	headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    	URI uri = UriComponentsBuilder.fromUriString(userInfoUri).build().toUri();
    	
    	RequestEntity<?> request;
    	headers.setBearerAuth(refreshToken);
    	request = new RequestEntity<>(headers, HttpMethod.GET, uri);
    	
    	
    	RestTemplate restTemplate = new RestTemplate();
    	RestOperations restOperations = restTemplate;
    	
    	ResponseEntity<Map> response = restTemplate.exchange(request, Map.class);
    	ResponseEntity<Map<String, Object>> response2 = restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
    	Map<String, Object> userAttributes = response.getBody();
    	Map<String, Object> userAttibutes2 = response2.getBody();
    	Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    	authorities.add(new OAuth2UserAuthority(userAttributes));
		OAuth2AccessToken token = auth2AuthorizedClient.getAccessToken();
		for (String authority : token.getScopes()) {
			authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
		}
    	
    	return ResponseEntity.ok(userAttributes);
    }
    
	@GetMapping("/login_success")
	public ResponseEntity<?> loginSuccesssfull(OAuth2AuthenticationToken authenticationToken) {
		//retrieve the current client base on loading the registration id and name field which are retrieved from the token
			OAuth2AuthorizedClient client = auth2AuthorizedClientService.loadAuthorizedClient(
											authenticationToken.getAuthorizedClientRegistrationId(),
											authenticationToken.getName()
											);
			
		//grab the user info endpoint, this user endpoint surely return to the json data of current client by using access token
			String userInfoUrl = client.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
			
			if(userInfoUrl.isEmpty()) {
				throw new OAuth2AuthenticationException("User info endpoint cannot be empty");
			}
				
				RestTemplate restTemplate = new RestTemplate();
				//send request for access api to Authorazation Server by sending it the access token
				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
				HttpEntity<String> entity = new HttpEntity<>("", headers); //write this header to the request
				ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, Map.class);
				Map userAttributes = response.getBody();
					
			
			
			return ResponseEntity.ok(userAttributes);
	}

}
