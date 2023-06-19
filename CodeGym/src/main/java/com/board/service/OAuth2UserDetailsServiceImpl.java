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
		String providerId="";

		OAuth2UserInfo oAuth2UserInfo = null;
		
		//구글에서 인증 후에 보내주는 데이터를 가져 옴. 데이터는 key,value 구조 되어 있음

		String provider = userRequest.getClientRegistration().getRegistrationId(); //구글 네이버 카카오 다
		String id= "";
		String email = "";	
		String name = "";
		String picture ="";
		
//
//		if(provider.equals("google")) {
//			// 구글은 sub 
//			providerId = oAuth2User.getAttribute("sub");
//		} else if(provider.equals("kakao")) {
//			//providerId = oAuth2User.getAttribute("id");
//		} else if(provider.equals("naver")) {
//			providerId = oAuth2User.getAttribute("id");
//		}

		oAuth2User.getAttributes().forEach((k,v) -> 
		{System.out.println(provider+" 로그인 주는거 forEach ==== " + k + " : " + v );});
		
		UserVO user = new UserVO();

		if(provider.equals("google")) {
			email = oAuth2User.getAttribute("email");
			name = oAuth2User.getAttribute("name");
			id = email + "_G";
			picture = oAuth2User.getAttribute("picture");
			
			if(service.idCheck(id) == 0) {
				System.out.println("id=="+id);
				System.out.println("email=="+email);
				System.out.println("setEmail=="+email);
				System.out.println("name=="+name);
				System.out.println("pwdEncoder.encode(\"tmeppw\")"+pwdEncoder.encode("tmeppw"));
				user.setUserid(id);
				user.setEmail(email);
				user.setUsername(name);
				user.setOrg_filename(picture);
				user.setPassword(pwdEncoder.encode("tmeppw"));
				user.setRole("USER");
				user.setFromsocial("G"); // 구글이라 G	
				System.out.println("user=-="+user);
				service.googleSignup(user);
			}
		} else if(provider.equals("naver")) {
			oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
			email = oAuth2UserInfo.getEmail();	
			name = oAuth2UserInfo.getName();
//			picture = oAuth2UserInfo.getPicture();
			
			id = email + "_N";
			System.out.println("**************** provider =" + provider);
			System.out.println("**************** email =" + email);
			
			if(service.idCheck(id) == 0) {
				System.out.println("id=="+id);
				System.out.println("email=="+email);
				System.out.println("setEmail=="+email);
				System.out.println("name=="+name);
				System.out.println("pwdEncoder.encode(\"tmeppw\")"+pwdEncoder.encode("tmeppw"));
				user.setUserid(id);
				user.setEmail(email);
				user.setUsername(name);
//				user.setOrg_filename(picture);
				user.setPassword(pwdEncoder.encode("tmeppw"));
				user.setRole("USER");
				user.setFromsocial("N"); // 구글이라 G	
				System.out.println("user=-="+user);
				service.naverSignup(user);
			}
		} else if(provider.equals("kakao")) {
			oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
			email = oAuth2UserInfo.getEmail();	
			name = oAuth2UserInfo.getName();
			id = email + "_K";
			System.out.println("**************** provider =" + provider);
			System.out.println("**************** email =" + email);
		
			if(service.idCheck(id) == 0) {
				System.out.println("id=="+id);
				System.out.println("email=="+email);
				System.out.println("name=="+name);
				System.out.println("pwdEncoder.encode(\"tmeppw\")"+pwdEncoder.encode("tmeppw"));
				user.setUserid(id);
				user.setEmail(email);
				user.setUsername(name);
				user.setPassword(pwdEncoder.encode("tmeppw"));
				user.setRole("USER");
				user.setFromsocial("K"); // 구글이라 G	
				System.out.println("user=-="+user);
				service.kakaoSignup(user);
			}
		}
		
		
		
		// UserVo 안에 userinfo 넣기
		UserVO OAuthUser = service.userinfo(id);

		//사용자 role 정보를 grantedAuthorities 리스트 객체에 저장
		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(OAuthUser.getRole());
		grantedAuthorities.add(grantedAuthority);

		// 사용자 정보를 VO에 넣음
		UserOAuth2VO userOAuth2VO = new UserOAuth2VO(id, OAuthUser.getPassword(), grantedAuthorities);

		userOAuth2VO.setAttribute(oAuth2User.getAttributes());
		userOAuth2VO.setAuthoroties(grantedAuthorities);
	    userOAuth2VO.setName(OAuthUser.getUsername());
	    
	    // 구글로그인 세션 만들기
		session.setAttribute("userid", id);
		session.setAttribute("username", OAuthUser.getUsername());
		session.setAttribute("role", OAuthUser.getRole());
		session.setMaxInactiveInterval(3600*24*7);
	    
		return userOAuth2VO;
	}

}
