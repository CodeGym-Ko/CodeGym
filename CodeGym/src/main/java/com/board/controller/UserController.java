package com.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

	@GetMapping("/user/login")
	public void getLogin() {}
	
	@PostMapping("/user/login")
	public String postLogin() {
	
//		//아이디 존재 여부 확인
//		if(service.idCheck(loginData.getUserid()) == 0)
//			return "{\"message\":\"ID_NOT_FOUND\"}";
//		
//		//아이디가 존재하면 읽어온 userid로 로그인 정보 가져 오기
//		UserVO member = service.userinfo(loginData.getUserid());
//		
//		//패스워드 확인
//		if(!pwdEncoder.matches(loginData.getPassword(),member.getPassword())) {
//			return "{\"message\":\"PASSWORD_NOT_FOUND\"}";
//		}else { //패스워드가 존재
//			
//		//세션 생성
//		session.setMaxInactiveInterval(3600*7);
//		session.setAttribute("userid", member.getUserid());
//		session.setAttribute("username", member.getUsername());
//		session.setAttribute("role", member.getRole());
//
//		return "{\"message\":\"good\",\"authkey\":\"" + member.getAuthkey() + "\"}";
//		}
		
		return "{\"message\":\"connect\"}";
	
	}
	//로그아웃
	@GetMapping("/user/logout")
	public String getLogout(HttpSession session) throws Exception {
		
		session.invalidate();
		return "redirect:/";
	}
}
