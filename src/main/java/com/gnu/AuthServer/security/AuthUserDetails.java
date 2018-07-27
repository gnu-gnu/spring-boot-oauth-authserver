package com.gnu.AuthServer.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gnu.AuthServer.entity.UserEntity;

public class AuthUserDetails implements UserDetails {
	private static final long serialVersionUID = 106806877220707136L;
	private UserEntity entity;
	
	public AuthUserDetails(UserEntity entity) {
		super();
		this.entity = entity;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		entity.getAuthorities().forEach(value -> {
			authorities.add(new SimpleGrantedAuthority(value.getRoleName()));
		});
		return authorities;
	}
	
	public UserEntity getEntity() {
		return entity;
	}

	public void setEntity(UserEntity entity) {
		this.entity = entity;
	}

	@Override
	public String getPassword() {
		return entity.getPassword();
	}

	@Override
	public String getUsername() {
		return entity.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
}
