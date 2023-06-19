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
	
	private Map<String, Object> attributesResponse; // 네이버용
	
	// 카카오용
	private Map<String, Object> attributes;
    private Map<String, Object> attributesAccount;
    private Map<String, Object> attributesProfile;
    
	private Collection<GrantedAuthority> authoroties;
	private String name;
	private String email;

	//구글용
	public UserOAuth2VO(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
	}
	// 네이버용
	 public UserOAuth2VO(String username, String password, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
	        super(username, password, authorities);
	        //this.attributes = attributes;
	        this.name = (String) attributes.get("name");
	        this.email = (String) attributes.get("email");
	 }
	 //카카오용
//	 public UserOAuth2VO(Map<String, Object> attributes) {
//	        super(username, password, authorities);
//	        this.attributes = attributes;
//	        this.attributesAccount = (Map<String, Object>) attributes.get("kakao_account");
//	        this.attributesProfile = (Map<String, Object>) attributesAccount.get("profile");
//	 }
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
