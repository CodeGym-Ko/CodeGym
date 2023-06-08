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
import com.board.dto.FileVO;
import com.board.dto.UserVO;
import com.board.service.BoardService;
import com.board.service.UserService;
import com.board.util.Page;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	
	@Autowired
	UserService service;
	
	@Autowired
	BoardService boardService;
	
	@Autowired //비밀번호 암호화 의존성 주입
	private BCryptPasswordEncoder pwdEncoder; 

	//로그인
	@GetMapping("/user/login")
	public void getLogin() {}
	

	@ResponseBody
	@PostMapping("/user/login")
	public String postLogIn(UserVO loginData,HttpSession session,@RequestParam("autologin") String autologin) {
		System.out.println("==== Post /user/login ==");
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
		UserVO user = service.userinfo(loginData.getUserid());
		
		//패스워드 확인
		if(!pwdEncoder.matches(loginData.getPassword(),user.getPassword())) {
			return "{\"message\":\"PASSWORD_NOT_FOUND\"}";
		}else { //패스워드가 존재
			
		//세션 생성
		session.setMaxInactiveInterval(3600*7);
		session.setAttribute("userid", user.getUserid());
		session.setAttribute("username", user.getUsername());
		session.setAttribute("role", user.getRole());
		
		System.out.println("==== 로그인 성공 == DB에서 가져온 userid : "+ user.getUserid());
		return "{\"message\":\"good\",\"authkey\":\"" + user.getAuthkey() + "\"}";
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
			
			String path = "c:\\Repository\\profile\\"; 
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
			int startPoint = (pageNum -1)*postNum+1; //테이블에서 읽어 올 행의 위치
			int endPoint = pageNum*postNum;
			int listCount = 10;
			
			Page page = new Page();
			
			int totalCount = service.addrTotalCount(addrSearch);
			List<AddressVO> list = new ArrayList<>();
			list = service.addrSearch(startPoint, endPoint, addrSearch);

			model.addAttribute("list", list);
			model.addAttribute("pageListView", page.getPageAddress(pageNum, postNum, listCount, totalCount, addrSearch));
			
		}
		
		//회원정보 보기
		@GetMapping("/user/userinfo")
		public void getUserInfo(Model model, HttpSession session) { 
			
			String session_userid = (String)session.getAttribute("userid");
			model.addAttribute("user", service.userinfo(session_userid));
			System.out.println("model : " + model);
			
		}
		
		//사용자 정보 수정 보기
		@GetMapping("/user/userInfoModify")
		public void getMemberInfoModify(Model model,HttpSession session) {
			
			String userid = (String)session.getAttribute("userid");
			UserVO user = service.userinfo(userid);
			//UserVO user_date = service.welcomeView(userid);
			System.out.println(" === Get /user/userInfoModify userid : " + userid);
			model.addAttribute("user", user);
			System.out.println("model : " + model);
			//model.addAttribute("user_date", user_date);
		}
		
		//사용자 정보 수정
		@PostMapping("/user/userInfoModify")
		public String postuserInfoModify(UserVO user,
				@RequestParam("fileUpload") MultipartFile multipartFile ) {
		System.out.println("===== Post /user/userInfoModify === user : "+user.toString());
		
		String path = "c:\\Repository\\profile\\";
		File targetFile;
		
		if(!multipartFile.isEmpty()) {

			//기존 프로파일 이미지 삭제
			UserVO vo = new UserVO();
			vo = service.userinfo(user.getUserid());
			File file = new File(path + vo.getStored_filename());
			file.delete();
			
			String org_filename = multipartFile.getOriginalFilename();	
			String org_fileExtension = org_filename.substring(org_filename.lastIndexOf("."));	
			String stored_filename =  UUID.randomUUID().toString().replaceAll("-", "") + org_fileExtension;	
							
			try {
				targetFile = new File(path + stored_filename);
				
				multipartFile.transferTo(targetFile);
				
				user.setOrg_filename(org_filename);
				user.setStored_filename(stored_filename);
				user.setFilesize(multipartFile.getSize());
																			
			} catch (Exception e ) { e.printStackTrace(); }
				
		}	

			service.userInfoUpdate(user);
			return "redirect:/user/userinfo";
			
		}
		
		//회원탈퇴
		@GetMapping("/user/userInfoDelete")
		public String getDeleteMember(HttpSession session) throws Exception {
			String userid = (String)session.getAttribute("userid"); 
			
			String path_profile = "c:\\Repository\\profile\\";
			String path_file = "c:\\Repository\\file\\";
			
			//회원 프로필 사진 삭제
			UserVO user= new UserVO();
			user = service.userinfo(userid);		
			File file = new File(path_profile + user.getStored_filename());
			file.delete();
			
			//회원이 업로드한 파일 삭제
			List<FileVO> fileList = boardService.fileInfoByUserid(userid);
			for(FileVO vo:fileList) {
				File f = new File(path_file + vo.getStored_filename());
				f.delete();
			}
			//게시물,댓글,파일업로드 정보, 회원정보 전체 삭제
			service.userInfoDelete((String)session.getAttribute("userid"));
			
			session.invalidate();
			System.out.println("=== Get 회원 탈퇴 완료 확인");
			return "redirect:/";
		}
		
		//사용자 패스워드 변경 페이지 보기
		@GetMapping("/user/userPasswordModify")
		public void getMemberPasswordModify() { }
		
		//사용자 패스워드 변경 
		@PostMapping("/user/userPasswordModify")
		public String postMemberPasswordModify(@RequestParam("old_userpassword") String old_password,
				@RequestParam("new_userpassword") String new_password, HttpSession session) { 
			
			String userid = (String)session.getAttribute("userid");
			
			UserVO member = service.userinfo(userid);
			if(pwdEncoder.matches(old_password, member.getPassword())) {
				member.setPassword(pwdEncoder.encode(new_password));
				service.passwordUpdate(member);
			}	
			return "redirect:/user/logout";
		}
		
		
}
