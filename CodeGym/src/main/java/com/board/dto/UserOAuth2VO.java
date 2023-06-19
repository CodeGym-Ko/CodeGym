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
	private Map<String,Object> attribute;//OAuth2User.getAttributes();
	private Collection<GrantedAuthority> authoroties;
	private String name;
	private String email;

	public UserOAuth2VO(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
	}	
	 public UserOAuth2VO(String username, String password, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
	        super(username, password, authorities);
	        //this.attributes = attributes;
	        this.name = (String) attributes.get("name");
	        this.email = (String) attributes.get("email");
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
