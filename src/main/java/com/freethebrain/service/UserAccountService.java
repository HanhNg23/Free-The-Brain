package com.freethebrain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.freethebrain.model.User;
import com.freethebrain.repository.UserRepository;

@Service
public class UserAccountService {
	
	@Autowired
	UserRepository userRepo;
	
	public boolean existedEmail(String email) {
		User user = userRepo.findByEmail(email).orElse(null);
		return user == null ? false : true;
	}
	
	public boolean existedId(String id) {
		User user = userRepo.findByEmail(id).orElse(null);
		return user == null ? false : true;
	}
}
