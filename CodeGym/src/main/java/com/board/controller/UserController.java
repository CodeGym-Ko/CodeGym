package com.board.controller;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.board.dto.AddressVO;
import com.board.dto.UserVO;
import com.board.service.UserService;
import com.board.util.Page;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

	@Autowired
	UserService service;
	
	@Autowired //비밀번호 암호화 의존성 주입
	public BCryptPasswordEncoder pwdEncoder; 

	//로그인
	@GetMapping("/user/login")
	public void getLogin(Model model) {}
	
	@ResponseBody
	@PostMapping("/user/login")
	public String postLogIn(UserVO loginData,HttpSession session,@RequestParam("autologin") String autologin) {
		
		String authkey = "";
		
		//로그인 시 자동 로그인 체크할 경우 신규 authkey 등록
		if(autologin.equals("NEW")) { 	 
			authkey = UUID.randomUUID().toString().replaceAll("-", ""); 
			loginData.setAuthkey(authkey);
			service.authkeyUpdate(loginData);	
		}
		
		//authkey가 클라이언트에 쿠키로 존재할 경우 로그인 과정 없이 세션 생성 후 게시판 목록 페이지로 이동  
		if(autologin.equals("PASS")) {
			
			UserVO userinfo = service.userinfoByAuthkey(loginData.getAuthkey());
			if(userinfo != null) {
				
				//세션 생성
				session.setMaxInactiveInterval(3600*7);
				session.setAttribute("userid", userinfo.getUserid());
				session.setAttribute("username", userinfo.getUsername());
				session.setAttribute("role", userinfo.getRole());
				
				return "{\"message\":\"good\"}";
			}else 
				return "{\"message\":\"bad\"}";
		}

		//아이디 존재 여부 확인
		if(service.idCheck(loginData.getUserid()) == 0)
			return "{\"message\":\"ID_NOT_FOUND\"}";
		
		//아이디가 존재하면 읽어온 userid로 로그인 정보 가져 오기
		UserVO member = service.userinfo(loginData.getUserid());
		
		//패스워드 확인
		if(!pwdEncoder.matches(loginData.getPassword(),member.getPassword())) {
			return "{\"message\":\"PASSWORD_NOT_FOUND\"}";
		}else { //패스워드가 존재
			
		//세션 생성
		session.setMaxInactiveInterval(3600*7);
		session.setAttribute("userid", member.getUserid());
		session.setAttribute("username", member.getUsername());
		session.setAttribute("role", member.getRole());

		return "{\"message\":\"good\",\"authkey\":\"" + member.getAuthkey() + "\"}";
		}
	}
	
	//로그아웃
	@GetMapping("/user/logout")
	public String getLogout(HttpSession session) throws Exception {
		
		session.invalidate();
		return "redirect:/";
	}
	
	//회원 가입
	@GetMapping("/user/signup")
	public void getSignup() throws Exception { }
	
	//회원 가입
	@ResponseBody
	@PostMapping("/user/signup")
	public String postSignup(UserVO user, @RequestParam("fileUpload") MultipartFile mpr) throws Exception {
		
		String path = "/Users/sangkyeongkim/Documents/Repository/profile"; 
		String org_filename = "";
		long filesize = 0L;
		
		if(!mpr.isEmpty()) {
			File targetFile = null; 
				
			org_filename = mpr.getOriginalFilename();	
			String org_fileExtension = org_filename.substring(org_filename.lastIndexOf("."));	
			String stored_filename = UUID.randomUUID().toString().replaceAll("-", "") + org_fileExtension;	
			filesize = mpr.getSize();
			targetFile = new File(path + stored_filename);
			mpr.transferTo(targetFile);	//raw data를 targetFile에서 가진 정보대로 변환
			user.setOrg_filename(org_filename);
			user.setStored_filename(stored_filename);
			user.setFilesize(filesize);
		}
		
		user.setPassword(pwdEncoder.encode(user.getPassword()));
		
		service.signup(user);		
		return "{\"username\":\"" + URLEncoder.encode(user.getUsername(),"UTF-8") + "\",\"status\":\"good\"}";
		//{"username": "김철수", "status": "good"}
		
	}
	
	//회원정보 보기
	@GetMapping("/user/userinfo")
	public void getUserInfo(Model model, HttpSession session) { 
		
		String session_userid = (String)session.getAttribute("userid");
		model.addAttribute("user", service.userinfo(session_userid));
		
	}
	
	//아이디 중복 체크
	@ResponseBody
	@PostMapping("/user/idCheck")
	public int getIdCheck(@RequestBody String userid) {
		
		return service.idCheck(userid);
		
	}
	
	//주소검색
	@GetMapping("/user/addrSearch")
	public void getSearchAddr(@RequestParam("addrSearch") String addrSearch,
			@RequestParam("page") int pageNum,Model model) throws Exception {
		
		int postNum = 5;
		int startPoint = (pageNum-1)*postNum+1; //테이블에서 읽어 올 행의 위치
		int endPoint = pageNum*postNum;
		int listCount = 10;
		
		Page page = new Page();
		
		int totalCount = service.addrTotalCount(addrSearch);
		List<AddressVO> list = new ArrayList<>();
		list = service.addrSearch(startPoint, endPoint, addrSearch);

		model.addAttribute("list", list);
		model.addAttribute("pageListView", page.getPageAddress(pageNum, postNum, listCount, totalCount, addrSearch));
		
	}
	
	
}
