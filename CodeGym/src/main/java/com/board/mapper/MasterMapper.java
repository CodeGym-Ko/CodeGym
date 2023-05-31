package com.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.board.dto.FileVO;

@Mapper
public interface MasterMapper {

	//삭제 파일 목록 갯수
	public int filedeleteCount();
	
	//삭제 파일 목록 정보
	public List<FileVO> filedeleteList();
	
	//파일 삭제
	public void deleteFile(int fileseqno);	
	
}
