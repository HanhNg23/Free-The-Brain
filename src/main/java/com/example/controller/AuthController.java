package com.example.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.exception.BadRequestException;
import com.example.model.AuthProvider;
import com.example.model.User;
import com.example.payload.ApiResponse;
import com.example.payload.AuthResponse;
import com.example.payload.LoginRequest;
import com.example.payload.SignupRequest;
import com.example.repository.UserRepository;
import com.example.security.JwtProvider;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtProvider jwtProvider;
	
	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getEmail(),
						loginRequest.getPassword())
				);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String accessToken = jwtProvider.createAccessToken(authentication);
		String refreshToken = jwtProvider.createAccessToken(authentication);
		
		return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));

	}
	 @PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest){
		if(userRepository.existsByEmail(signUpRequest.getEmail())) {
			throw new BadRequestException("Email address already in use.");
		}
		
		User user  = User.builder()
				.accountName(signUpRequest.getName())
				.email(signUpRequest.getEmail())
				.password(passwordEncoder.encode(signUpRequest.getPassword()))
				.provider(AuthProvider.LOCAL)
				.build();
		
		User savedUser = userRepository.save(user);
		
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/me")
				.buildAndExpand(savedUser.getId()).toUri();
		
		return ResponseEntity.created(location).body(new ApiResponse(true, "User Registered Successfully"));
		
		
	}
	
}
