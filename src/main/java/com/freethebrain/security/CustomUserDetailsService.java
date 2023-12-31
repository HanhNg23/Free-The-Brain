package com.freethebrain.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.freethebrain.exception.ResourceNotFoundException;
import com.freethebrain.model.User;
import com.freethebrain.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
//this user will be stored in principal of Authentication in
//SecurityContext after authenticated successfully
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email :" + email));
		return UserPrincipal.create(user);
	}

	@Transactional
	public UserDetails loadUserById(String id) {
		User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
		return UserPrincipal.create(user);
	}

}
