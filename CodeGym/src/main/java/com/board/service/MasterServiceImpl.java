package com.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.board.dto.FileVO;
import com.board.mapper.MasterMapper;

@Service
public class MasterServiceImpl implements MasterService{

	@Autowired
	MasterMapper mapper;
	
	//삭제 파일 목록 갯수
	@Override
	public int filedeleteCount() {
		return mapper.filedeleteCount();
	}
	
	//삭제 파일 목록 정보
	@Override
	public List<FileVO> filedeleteList(){
		return mapper.filedeleteList();
	}
	
	//파일 삭제
	@Override
	public void deleteFile(int fileseqno) {
		mapper.deleteFile(fileseqno);
	}
	
}
