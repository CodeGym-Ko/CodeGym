package com.board;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.board.dto.UserVO;
import com.board.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{

	private final UserService service;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, 
			HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		
		UserVO userInfo = service.userinfo(authentication.getName());
		
		//세션 생성
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(3600*24*7);
		session.setAttribute("userid", userInfo.getUserid());
		session.setAttribute("username", userInfo.getUsername());
		session.setAttribute("role", userInfo.getRole());
		System.out.println(" 시큐리티config 통해서 로그인 성공 !!! userid : "+session.getId());
		
		setDefaultTargetUrl("/");
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
