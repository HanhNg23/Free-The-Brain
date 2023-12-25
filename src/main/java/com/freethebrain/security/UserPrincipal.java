package com.freethebrain.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.freethebrain.model.Role;
import com.freethebrain.model.User;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

//OAuth2User - representation of a user Principal that is registered with an OAuth 2.0Provider //
public class UserPrincipal implements OAuth2User, UserDetails {

	private String id;
	private String email;
	private String password;
	@Enumerated(EnumType.STRING)
	private Role role;
	private Map<String, Object> attributes;// storing extend attributes

	public UserPrincipal(String id, String email, String password, Role role) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	// for authentication with email and password
	public static UserPrincipal create(User user) {
		return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.getRole());
	}

	// for authetication with OAuth2
	public static UserPrincipal create(User user, Map<String, Object> attributes) {
		UserPrincipal userPrincipal = UserPrincipal.create(user);
		userPrincipal.setAttributes(attributes);
		return userPrincipal;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		GrantedAuthority grandAuthority = new SimpleGrantedAuthority(this.role.name());
		// only one role pass to SimpleGrantedAutority
		// if one more role apply for one account, let
		// create the list of grandAuthority and return
		// the list of GrantedAuthoriy objects instead of singletoList()
		return Collections.singletonList(grandAuthority);
	}

	public String getId() {
		return String.valueOf(id);
	}

	public String getEmail() {
		return email;
	}

	public Role getRole() {
		return role;
	}

	@Override
	public String getName() {
		return this.email;
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
