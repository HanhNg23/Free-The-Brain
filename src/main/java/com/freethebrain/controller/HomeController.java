package com.freethebrain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.freethebrain.exception.ResourceNotFoundException;
import com.freethebrain.model.User;
import com.freethebrain.payload.ApiResponse;
import com.freethebrain.repository.UserRepository;
import com.freethebrain.security.CurrentUser;
import com.freethebrain.security.UserPrincipal;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class HomeController {
	   @Autowired
	    private UserRepository userRepository;
	//@GetMapping("/home")
	public ResponseEntity<String> goHome() {
		System.out.println("HOMEEEEEEE");
		return ResponseEntity.ok("HomePage");
	}
	
	@GetMapping("/home")
	public String goHomePage() {
		return "HomePage";
	}
	
//	@GetMapping("/small/home")
//	public ResponseEntity<String> secretSource(){
//		
//		return ResponseEntity.ok().body("<h1>This is the secret</h1>"); 
//	}
	@GetMapping("/small/home")
	public ApiResponse<?> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
		User user = userRepository.findById(userPrincipal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
		return new ApiResponse<User>(HttpStatus.OK, user);
	}
}
