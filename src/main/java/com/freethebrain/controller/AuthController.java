package com.freethebrain.controller;

import java.net.URI;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
import com.freethebrain.config.AppProperties;
import com.freethebrain.exception.BadRequestException;
import com.freethebrain.model.AuthProvider;
import com.freethebrain.model.Role;
import com.freethebrain.model.User;
import com.freethebrain.payload.ApiResponse;
import com.freethebrain.payload.AuthResponse;
import com.freethebrain.payload.LoginRequest;
import com.freethebrain.payload.SignupRequest;
import com.freethebrain.repository.UserRepository;
import com.freethebrain.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    
    @Autowired
    private AppProperties appProperties;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
 
    @PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getEmail(),
						loginRequest.getPassword())
				);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String accessToken = jwtProvider.createAccessToken(authentication);
		String refreshToken = jwtProvider.createAccessToken(authentication);
		
		//Tradition add cookie
		Cookie accessCookieToken = new Cookie(appProperties.getAuth().getAccessTokenName(), accessToken);
		accessCookieToken.setMaxAge((int) appProperties.getAuth().getAccessTokenExpirationMsec() / 1000);
		accessCookieToken.setHttpOnly(true);
		accessCookieToken.setPath("/");
		accessCookieToken.setSecure(true);
		response.addCookie(accessCookieToken);
		
		ResponseCookie refreshCookieToken = ResponseCookie
				.from(appProperties.getAuth().getRefreshTokenName(), refreshToken)
				.maxAge(appProperties.getAuth().getRefreshTokenExpirationMsec() / 1000)
				.httpOnly(true)
				.path("/")
				.secure(true)
				.build();
	
		ResponseCookie deleteCookie1 = ResponseCookie.from("Authorization-Tradition", "null").maxAge(0).build();
		ResponseCookie deleteCookie2 = ResponseCookie.from("Authorization-au", "null").maxAge(30).build();
				
		return ResponseEntity.ok()
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.header(HttpHeaders.SET_COOKIE, accessCookieToken.toString())
				.header(HttpHeaders.SET_COOKIE, refreshCookieToken.toString())
				.header(HttpHeaders.SET_COOKIE, deleteCookie1.toString())
				.header(HttpHeaders.SET_COOKIE, deleteCookie2.toString())
				.body(new AuthResponse(accessToken, refreshToken));

	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

		System.out.println("HELLOOOOOOOOOOOOOOOO " + signUpRequest.toString());
		logger.trace("----USER REQUIRES SIGN UP: {}", signUpRequest);
		System.out.println("HELLOOOOOOOOOOOOOOOO " + userRepository.findByEmail(signUpRequest.getEmail()).orElse(null));
		User existedUser = userRepository.findByEmail(signUpRequest.getEmail()).orElse(null);
		
		if(existedUser != null) 
			throw new BadRequestException("Email address already in use");
		
		User user = User
				.builder()
				.accountName(signUpRequest.getAccoutName())
				.email(signUpRequest.getEmail())
				.password(passwordEncoder.encode(signUpRequest.getPassword()))
				.provider(AuthProvider.LOCAL)
				.dateCreated(LocalDateTime.now())
				.emailVerified(false)
				.role(Role.PLAYER)
				.build();

		User savedUser = userRepository.save(user);

		URI location = ServletUriComponentsBuilder
				.fromCurrentContextPath()
				.path("/user/me")
				.buildAndExpand(savedUser.getId())
				.toUri();//http://localhost:8080/user/me/{id}
		
		return ResponseEntity.created(location).body(new ApiResponse(true, "User Registered Successfully", location.toString()));
	}
	
	@PostMapping("/refresh-token")
	public ResponseEntity<?> refreshToken(String accessToken){
		return null;
	}
	
	@RequestMapping("/logout")
	public ResponseEntity<?> logout(){
		return null;
	}
	
}
