package com.board.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.board.dto.AddressVO;
import com.board.dto.UserVO;
import com.board.mapper.UserMapper;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserMapper mapper;
	
	//아이디 중복 체크
	@Override
	public int idCheck(String userid) {
		return mapper.idCheck(userid);
	}

	//로그인 정보 가져 오기
	@Override
	public UserVO login(String userid) {
		return mapper.login(userid);
	}

	//사용자 등록
	@Override
	public void signup(UserVO user) {
		mapper.signup(user);		
	}
	
	//사용자 자동 로그인을 위한 authkey 등록
	@Override
	public void authkeyUpdate(UserVO user) {
		mapper.authkeyUpdate(user);
	}
	
	//사용자 자동 로그인을 위한 authkey로 사용자 정보 가져 오기 
	@Override
	public UserVO userinfoByAuthkey(String authkey) {
		return mapper.userinfoByAuthkey(authkey);
	}
	
	//사용자 정보 보기
	@Override
	public UserVO userinfo(String userid) {
		return mapper.userinfo(userid);
	}
	
	//주소 전체 갯수 계산
	@Override
	public int addrTotalCount(String addrSearch) {
		return mapper.addrTotalCount(addrSearch);
	}

	//주소 검색
	@Override
	public List<AddressVO> addrSearch(int startPoint, int endPoint, String addrSearch){
		Map<String,Object> data = new HashMap<>();
		data.put("startPoint", startPoint);
		data.put("endPoint", endPoint);
		data.put("addrSearch", addrSearch);
		return mapper.addrSearch(data);
	}
	
	//사용자 정보 수정
	@Override
	public void userInfoUpdate(UserVO user) {
		mapper.userInfoUpdate(user);		
	}

	//회원 탈퇴
	@Override
	public void userInfoDelete(String userid) {
		mapper.userInfoDelete(userid);		
	}
	
	//사용자 패스워드 변경
	@Override
	public void passwordUpdate(UserVO user) {
		mapper.passwordUpdate(user);		
	}
}
