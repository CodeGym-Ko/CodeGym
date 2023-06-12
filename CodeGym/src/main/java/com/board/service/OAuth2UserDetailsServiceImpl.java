package com.board.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.board.dto.UserOAuth2VO;
import com.board.dto.UserVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OAuth2UserDetailsServiceImpl extends DefaultOAuth2UserService{
	
	private final UserService service;
	private final PasswordEncoder pwdEncoder;
	private final HttpSession session;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		OAuth2User oAuth2User = super.loadUser(userRequest);
		
		//구글에서 인증 후에 보내주는 데이터를 가져 옴. 데이터는 key,value 구조 되어 있음
		String provider = userRequest.getClientRegistration().getRegistrationId();
		String providerId = oAuth2User.getAttribute("sub");
		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");
		
		System.out.println("**************** provider =" + provider);
		System.out.println("**************** providerId =" + providerId);
		System.out.println("**************** email =" + email);
		System.out.println("**************** name =" + name);
		
		oAuth2User.getAttributes().forEach((k,v) -> 
			{System.out.println("구글로그인 주는거 forEach ==== " + k + " : " + v );});
		
		
		UserVO user = new UserVO();
		
		//구글에서 받은 승인된 이메일로 사용자 정보 등록
		if(service.idCheck(email) == 0) {
			user.setUserid(email);
			user.setUsername(name);
			user.setPassword(pwdEncoder.encode("12345"));
			user.setRole("USER");
			user.setFromsocial("G");			
			service.googleSignup(user);
		}

		// UserVo 안에 userinfo 넣기
		UserVO googleUser = service.userinfo(email);

		//사용자 role 정보를 grantedAuthorities 리스트 객체에 저장
		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(googleUser.getRole());
		grantedAuthorities.add(grantedAuthority);

		UserOAuth2VO userOAuth2VO = 
				new UserOAuth2VO(email, googleUser.getPassword(), grantedAuthorities);

		userOAuth2VO.setAttribute(oAuth2User.getAttributes());
		userOAuth2VO.setAuthoroties(grantedAuthorities);
	    userOAuth2VO.setName(googleUser.getUsername());
	    
	    // 구글로그인 세션 만들기
		session.setAttribute("userid", email);
		session.setAttribute("username", googleUser.getUsername());
		session.setAttribute("role", googleUser.getRole());
		session.setMaxInactiveInterval(3600*24*7);
	    
		return userOAuth2VO;
	}

}
