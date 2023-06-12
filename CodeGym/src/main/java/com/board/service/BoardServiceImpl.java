package com.board.service;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.board.dto.BoardVO;
import com.board.dto.FileVO;
import com.board.dto.LikeVO;
import com.board.dto.ReplyVO;
import com.board.mapper.BoardMapper;

@Service
public class BoardServiceImpl implements BoardService {
	
	@Autowired
	BoardMapper mapper;

	//게시물 목록 보기
	@Override
	public List<BoardVO> list(int startPoint, int endPoint, String keyword) {
		Map<String,Object> data = new HashMap<>();
		data.put("startPoint", startPoint);
		data.put("endPoint", endPoint);
		data.put("keyword", keyword);		
		return mapper.list(data);
	}
	
	//공지사항
	@Override
	public List<BoardVO> notice(int startPoint, int endPoint, String keyword) {
		Map<String,Object> data = new HashMap<>();
		data.put("startPoint", startPoint);
		data.put("endPoint", endPoint);
		data.put("keyword", keyword);		
		return mapper.notice(data);
	}
	
	// 인덱스 페이지 hot 게시물 뽑기
	@Override
	public BoardVO hotBoard(String boardType) {
		return mapper.hotBoard(boardType);
	}
	
	//게시물 전체 갯수 계산
	@Override
	public int getTotalCount(String keyword) {
		return mapper.getTotalCount(keyword);
	}
	
	//게시물 번호 구하기
	@Override
	public int getSeqnoWithNextval() {
		return mapper.getSeqnoWithNextval();
	}

	//게시물 등록
	@Override
	public void write(BoardVO board) {
		mapper.write(board);
	}
	
	//파일 업로드 정보 등록
	@Override
	public void fileInfoRegistry(Map<String,Object> fileInfo) throws Exception{
		mapper.fileInfoRegistry(fileInfo);
	}

	//게시글 내에서 업로드된 파일 목록 보기
	@Override
	public List<FileVO> fileListView(int seqno) throws Exception{
		return mapper.fileListView(seqno);
	}

	//게시물 수정에서 파일 삭제
	@Override
	public void deleteFileList(Map<String,Object> data) throws Exception{
		mapper.deleteFileList(data);
	}
	
	//게시물 삭제에서 파일 삭제를 위한 파일 목록 가져 오기
	@Override
	public List<FileVO> deleteFileOnBoard(int seqno) throws Exception {
		return mapper.deleteFileOnBoard(seqno);
	}
	
	//회원 탈퇴 시 회원이 업로드한 파일 정보 가져 오기
	@Override
	public List<FileVO> fileInfoByUserid(String userid) throws Exception{
		return mapper.fileInfoByUserid(userid);
	}

	//다운로드를 위한 파일 정보 보기
	@Override
	public FileVO fileInfo(int fileseqno) throws Exception {
		return mapper.fileInfo(fileseqno);
	}

	//게시물 내용 보기
	@Override
	public BoardVO view(int seqno) {
		return mapper.view(seqno);
	}

	//이전 보기
	@Override
	public int pre_seqno(int seqno, String keyword) {
		Map<String,Object> data = new HashMap<>();
		data.put("seqno", seqno);
		data.put("keyword", keyword);
		return mapper.pre_seqno(data);
	}
	
	//다음 보기
	@Override
	public int next_seqno(int seqno, String keyword) {
		Map<String,Object> data = new HashMap<>();
		data.put("seqno", seqno);
		data.put("keyword", keyword);
		return mapper.next_seqno(data);
	}
	//조회수 등록
	@Override
	public void hitno(BoardVO board) {
		mapper.hitno(board);		
	}

	//게시물 수정
	@Override
	public void modify(BoardVO board) {
		mapper.modify(board);		
	}

	//게시물 삭제
	@Override
	public void delete(int seqno) {
		mapper.delete(seqno);		
	}
	
	//좋아요/싫어요 확인 가져 오기
	@Override
	public LikeVO likeCheckView(int seqno,String userid) throws Exception {
		Map<String,Object> data = new HashMap<>();
		data.put("seqno", seqno);
		data.put("userid", userid);
		return mapper.likeCheckView(data);
	}
	
	//좋아요/싫어요 갯수 수정하기
	@Override
	public void boardLikeUpdate(int seqno, int likecnt, int dislikecnt) throws Exception {
		Map<String,Object> data = new HashMap<>();
		data.put("seqno", seqno);
		data.put("likecnt", likecnt);
		data.put("dislikecnt", dislikecnt);
		mapper.boardLikeUpdate(data);
	}
	
	//좋아요/싫어요 확인 등록하기
	@Override
	public void likeCheckRegistry(Map<String,Object> map) throws Exception {
		mapper.likeCheckRegistry(map);
	}
	
	//좋아요/싫어요 확인 수정하기
	@Override
	public void likeCheckUpdate(Map<String,Object> map) throws Exception {
		mapper.likeCheckUpdate(map);
	}
	
	//댓글 보기
	@Override
	public List<ReplyVO> replyView(ReplyVO reply) throws Exception{
		return mapper.replyView(reply);
	}
	
	//댓글 수정
	@Override
	public void replyUpdate(ReplyVO reply) throws Exception{
		mapper.replyUpdate(reply);
	}
	
	//댓글 등록
	@Override
	public void replyRegistry(ReplyVO reply) throws Exception{
		mapper.replyRegistry(reply);
	}
	
	//댓글 삭제
	@Override
	public void replyDelete(ReplyVO reply) throws Exception{
		mapper.replyDelete(reply);
	}

}
