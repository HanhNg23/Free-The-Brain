package com.freethebrain.security.oauth2;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.freethebrain.exception.OAuth2AuthenticationProcessingException;
import com.freethebrain.model.AuthProvider;
import com.freethebrain.model.Role;
import com.freethebrain.model.User;
import com.freethebrain.repository.UserRepository;
import com.freethebrain.security.UserPrincipal;
import com.freethebrain.security.oauth2.userInfo.OAuth2UserInfo;
import com.freethebrain.security.oauth2.userInfo.OAuth2UserInfoFactory;

// DefaultOAuth2UserService will be work to send
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService { // this class has implements OAuth2UserService

    @Autowired
    private UserRepository userRepository;

    @Override
	public OAuth2User loadUser(OAuth2UserRequest oauth2userRequest) throws OAuth2AuthenticationException {
    	// Returns an OAuth2User after obtaining the user attributes of the End-User from the UserInfo End point
    	
    	OAuth2User oauth2User = super.loadUser(oauth2userRequest); 
		ProviderDetails pd = oauth2userRequest.getClientRegistration().getProviderDetails();
		System.out.println("PROVIDER DETAILS : " +
				"\n - " + pd.getUserInfoEndpoint().getUri() +
				"\n - " + pd.getUserInfoEndpoint().getUserNameAttributeName() +
				"\n - " + pd.getUserInfoEndpoint().getAuthenticationMethod().getValue() 
				);
		try {
			return processOAuth2User(oauth2userRequest, oauth2User);
		} catch (AuthenticationException ex) {
			throw ex;
		} catch (Exception ex) {
			// Throwing an instance of AuthenticationException will trigger
			// the OAuth2AuthenticationFailureHandler
			throw new InternalAuthenticationServiceException(ex.getMessage());
		}
	}

    private OAuth2User processOAuth2User(OAuth2UserRequest oauth2UserRequest, OAuth2User oauth2User)
	    throws OAuth2AuthenticationProcessingException {
	// Get OAth2UserInfor bases by pass registrationId parameter and Map<key,value> attributes of oauth2User.
    // The instance of OAuth2User get from the OAuth2UserRequest
    	System.out.println("--- OAuth2UserRequest: " 
	    + "\n - ClientRegistation: " + oauth2UserRequest.getClientRegistration().toString()
	    + "\n - AccessToken :: "  + oauth2UserRequest.getAccessToken().getTokenValue()
	    + "\n -- TokenType : "  + oauth2UserRequest.getAccessToken().getTokenType().getValue()
	    + "\n -- ExpiresAt : " + oauth2UserRequest.getAccessToken().getExpiresAt()
	    + "\n -- IssuedAt : " + oauth2UserRequest.getAccessToken().getIssuedAt()
	    + "\n -- Scopes : " + oauth2UserRequest.getAccessToken().getScopes().toString()
	    + "\n - Additional Parameters: " + oauth2UserRequest.getAdditionalParameters().toString());
    	
    OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oauth2UserRequest
    			.getClientRegistration().getRegistrationId(), oauth2User.getAttributes()); 

	System.out.println("OAUTH2 USER INFO: " + oauth2UserInfo.toString());


	if (oauth2UserInfo.getEmail().isEmpty()) {
	    throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
	}

	// if email not empty check if this email has existed
	Optional<User> userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail());

	User user;

	// If a value is present, returns true, otherwise false.
	// In here, means that user email found in system so just let them update existing User
	if (userOptional.isPresent()) {
	    user = userOptional.get();// If a value is present, returns the value, otherwise throws NoSuchElementException.
	    if (!user.getProvider().getAuthProvider()
		    .equalsIgnoreCase(oauth2UserRequest.getClientRegistration().getRegistrationId())) {
		throw new OAuth2AuthenticationProcessingException(
			"Looks like you're signed up with " + user.getProvider() + " account. Please use your "
				+ user.getProvider() + " account to login.");
	    }
	    user = updateExistingUser(user, oauth2UserInfo); 
	} else {
	    user = registerNewUser(oauth2UserRequest, oauth2UserInfo);
	}
	// UserPrincipal will store the current OAuth2 authenticated User
	return UserPrincipal.create(user, oauth2User.getAttributes());

    }

	private User registerNewUser(OAuth2UserRequest oauth2UserRequest, OAuth2UserInfo oauth2UserInfo) {
		User user = User
				.builder()
				.provider(AuthProvider.valueOf(AuthProvider.class, oauth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()))
				.providerId(oauth2UserInfo.getId())
				.accountName(oauth2UserInfo.getAccountName())
				.email(oauth2UserInfo.getEmail())
				.imageUrl(oauth2UserInfo.getImageurl())
				.emailVerified(true)
				.dateCreated(LocalDateTime.now())
				.role(Role.PLAYER)
				.build();
		return userRepository.save(user);
	}

	private User updateExistingUser(User existingUser, OAuth2UserInfo oauth2UserInfo) {
		existingUser.setAccountName(oauth2UserInfo.getAccountName());
		existingUser.setImageUrl(oauth2UserInfo.getImageurl());
		existingUser.setLastModified(LocalDateTime.now());
		return userRepository.save(existingUser);
	}

}
