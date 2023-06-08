package com.board.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.board.dto.AddressVO;
import com.board.dto.UserVO;

@Mapper
public interface UserMapper {

	//아이디 중복 체크. 카운터가 0이면 아이디 사용 가능, 1이면 기존 사용 중인 아이디
	public int idCheck(String userid);
	
	//로그인 정보 가져 오기
	public UserVO login(String userid);
	
	//사용자등록
	public void signup(UserVO user);
	
	//사용자 자동 로그인을 위한 authkey 등록
	public void authkeyUpdate(UserVO user);
	
	//사용자 자동 로그인을 위한 authkey로 사용자 정보 가져 오기 
	public UserVO userinfoByAuthkey(String authkey);
	
	//사용자 정보 보기
	public UserVO userinfo(String userid);
	
	//주소 전체 갯수 계산
	public int addrTotalCount(String addrSearch);

	//주소 검색
	public List<AddressVO> addrSearch(Map<String,Object> data);
	
	//사용자 정보 수정
	public void userInfoUpdate(UserVO user);
	
}