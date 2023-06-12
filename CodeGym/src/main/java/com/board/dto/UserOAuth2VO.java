package com.board.dto;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.*;

@Getter
@Setter
public class UserOAuth2VO extends User implements OAuth2User {

	private static final long serialVersionUID = 1L;
	private Map<String,Object> attribute;
	private Collection<GrantedAuthority> authoroties;
	private String name;

	public UserOAuth2VO(String username, String password, Collection<GrantedAuthority> authorities) {
		super(username, password, authorities);
	}	
	
	@Override
	public Map<String, Object> getAttributes() {
		return attribute;
	}

	@Override
	public String getName() {
        return name;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authoroties;
	}

}
