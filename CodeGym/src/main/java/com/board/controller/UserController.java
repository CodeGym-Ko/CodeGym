package com.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	
//	@Autowired
//	UserService service;
	
//	@Autowired //비밀번호 암호화 의존성 주입
//	private BCryptPasswordEncoder pwdEncoder; 
	
	@GetMapping("/user/login")
	public void getLogin() {}
	
	@PostMapping("/user/login")
//	public String postLogin(UserVO loginData,HttpSession session,@RequestParam("autologin") String autologin) {
	public String postLogin(HttpSession session,@RequestParam("autologin") String autologin) {
		String id = "aa";
		System.out.println(" ==== Post /user.login *** ?autologin : " + autologin );
		System.out.println("session : "+session);
		//아이디 존재 여부 확인
//		if(service.idCheck(loginData.getUserid()) == 0)
		//if(service.idCheck(loginData.getUserid()) == 0)
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
