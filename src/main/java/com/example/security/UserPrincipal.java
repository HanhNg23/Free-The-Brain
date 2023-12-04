package com.example.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.model.Role;
import com.example.model.User;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/*
 * This class is used to store user Principal = UserDetails in Principal contain identified User which registered with OAuth2User Provider */
public class UserPrincipal implements OAuth2User, UserDetails{

	private Long id;
	private String email;
	private String password;
	@Enumerated(EnumType.STRING)
	private Role role;
	private Map<String, Object> attributes;
	
	public UserPrincipal(Long id, String email, String password, Role role) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.role = role;
	}
	
	//for authentication with email and password
	public static UserPrincipal create(User user) {
		return new UserPrincipal(user.getId(),
								 user.getEmail(),
								 user.getPassword(),
								 user.getRole());
	}
	
	//for authetication with OAuth2
	public static UserPrincipal create(User user, Map<String, Object> attributes) {
		UserPrincipal userPrincipal = UserPrincipal.create(user);
		userPrincipal.setAttributes(attributes);
		return userPrincipal;
		
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		GrantedAuthority grandAuthority = new SimpleGrantedAuthority(this.role.name());
		return Collections.singletonList(grandAuthority);
	}

	
	
	public Long getId() {
		return id;
	}


	public String getEmail() {
		return email;
	}


	public Role getRole() {
		return role;
	}


	@Override
	public String getName() {
		return String.valueOf(this.id);
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}
	
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
 
}
