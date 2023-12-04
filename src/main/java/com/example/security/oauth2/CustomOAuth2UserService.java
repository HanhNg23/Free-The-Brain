package com.example.security.oauth2;

import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.example.exception.OAuth2AuthenticationProcessingException;
import com.example.model.AuthProvider;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.security.UserPrincipal;
import com.example.security.oauth2.user.OAuth2UserInfo;
import com.example.security.oauth2.user.OAuth2UserInfoFactory;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService { //this class has implements OAuth2UserService

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest oauth2userRequest) throws OAuth2AuthenticationException {
		
		OAuth2User oauth2User = super.loadUser(oauth2userRequest);
		
		try {
			return processOAuth2User(oauth2userRequest, oauth2User);
		}catch(AuthenticationException ex) {
			throw ex;
		}catch(Exception ex) {
			// Throwing an instance of AuthenticationException will trigger
			// the OAuth2AuthenticationFailureHandler
			throw new InternalAuthenticationServiceException(ex.getMessage());
		}
	}
	
	private OAuth2User processOAuth2User(OAuth2UserRequest oauth2UserRequest, OAuth2User oauth2User) 
			throws OAuth2AuthenticationProcessingException {
		//get OAtuh2UserInfor bases on registrationID
		OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
				oauth2UserRequest.getClientRegistration().getRegistrationId(),
				oauth2User.getAttributes()
				);
		
		System.out.println("CLIENT REGISTRATION: " +  oauth2UserRequest.getClientRegistration().toString());
		
		if(oauth2UserInfo.getEmail().isEmpty()) {
			throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
		}
		
		//if email not empty check if this email has existed
		Optional<User> userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail());
		
		User user;
		
		//If a value is present, returns true, otherwise false.
		//In here, means that user email found in system so just let them update existing User
		if(userOptional.isPresent()) {
			user = userOptional.get();//If a value is present, returns the value, otherwise throws NoSuchElementException.
			if(!user.getProvider()
					.equals(AuthProvider
							.valueOf(oauth2UserRequest.getClientRegistration().getRegistrationId()))) {
				throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
			}
			user = updateExistingUser(user,oauth2UserInfo);
		}else {
			user = registerNewUser(oauth2UserRequest, oauth2UserInfo);
		}
		//UserPrincipal will store the current OAuth2 authenticated User
		return UserPrincipal.create(user, oauth2User.getAttributes());
		
	}


	private User registerNewUser(OAuth2UserRequest oauth2UserRequest, OAuth2UserInfo oauth2UserInfo) {

		User user = User.builder()
				.provider(AuthProvider.valueOf(oauth2UserRequest.getClientRegistration().toString()))
				.providerId(oauth2UserInfo.getId())
				.accountName(oauth2UserInfo.getAccountName())
				.email(oauth2UserInfo.getEmail())
				.imageUrl(oauth2UserInfo.getImageurl()).build();
		return userRepository.save(user);
	}

	private User updateExistingUser(User existingUser, OAuth2UserInfo oauth2UserInfo) {
		existingUser.setAccountName(oauth2UserInfo.getAccountName());
		existingUser.setImageUrl(oauth2UserInfo.getImageurl());
		return userRepository.save(existingUser);
	}
	

}
