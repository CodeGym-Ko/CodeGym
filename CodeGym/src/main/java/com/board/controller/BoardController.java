package com.board.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.board.dto.BoardVO;
import com.board.dto.FileVO;
import com.board.dto.LikeVO;
import com.board.service.BoardService;
import com.board.util.Page;

import jakarta.servlet.http.HttpSession;

@Controller
public class BoardController {

	@Autowired
	BoardService service; // 의존성 주입

	@GetMapping("/exercise/freeWeight")
	public void getExercise() {
	}
	
	@GetMapping("/exercise/chest")
	public void getChest() {

	}
	@GetMapping("/exercise/abdominals")
	public void getAbs() {
		
	}

	@GetMapping("/nutritional/bulkup")
	public void getBulkup() {
	}

	@GetMapping("/nutritional/diet")
	public void getDiet() {
	}

	@GetMapping("/nutritional/bmi")
	public void getBmi() {
	}

	// 공지사항
	@GetMapping("/customerCenter/notice")
	public void getNotice(@RequestParam("page") int pageNum,
			@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword, Model model)
			throws Exception {
		// 목록 보기
		int postNum = 10; // 한 화면에 보여지는 게시물 행의 갯수
		int pageListCount = 10; // 화면 하단에 보여지는 페이지리스트 내의 페이지 갯수
		int startPoint = (pageNum - 1) * postNum + 1;
		int endPoint = pageNum * postNum;

		Page page = new Page();

		model.addAttribute("list", service.notice(startPoint, endPoint, keyword));
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);
		model.addAttribute("pageList", page.getPageList(pageNum, postNum, pageListCount, service.getTotalCount(keyword), keyword));
	}
	
	// 공지사항 작성 화면
	@GetMapping("/customerCenter/noticeWrite")
	public void getNoticeWrite() {
		
	}
	
	//첨부 파일 없는 공지사항 등록
	@ResponseBody
	@PostMapping("/customerCenter/noticeWrite")
	public String PostWrite(BoardVO board) throws Exception{

		int seqno = service.getSeqnoWithNextval();
		board.setSeqno(seqno);
		service.write(board);
		return "{\"message\":\"good\"}";

	}
	
	// 첨부 파일 있는 공지사항 등록
	@ResponseBody
	@PostMapping("/customerCenter/fileUpload")
		public String postFileUpload(BoardVO board,@RequestParam("SendToFileList") List<MultipartFile> multipartFile, 
				@RequestParam("kind") String kind,@RequestParam(name="deleteFileList", required=false) int[] deleteFileList) throws Exception{

			String path = "c:\\Repository\\file\\"; 
			int seqno = 0;
			
			if(kind.equals("I")) { //게시물 등록 시 게시물 등록 
				seqno = service.getSeqnoWithNextval();
				board.setSeqno(seqno);
				service.write(board);			
			}
			
			if(kind.equals("U")) { //게시물 수정 시 게시물 수정
				seqno = board.getSeqno();
				service.modify(board);
				
				if(deleteFileList != null) {
					
					for(int i=0; i<deleteFileList.length; i++) {

						//파일 삭제
						FileVO fileInfo = new FileVO();
						fileInfo = service.fileInfo(deleteFileList[i]);
						//File file = new File(path + fileInfo.getStored_filename());
						//file.delete();
						
						//파일 테이블에서 파일 정보 삭제
						//게시물 수정에서 삭제할 파일 목록이 전송되면 이 값을 받아서 tbl_file내에 있는 파일 정보를 하나씩 삭제하는 deleteFileList 실행
						Map<String,Object> data = new HashMap<>();
						data.put("kind", "F"); 
						data.put("fileseqno", deleteFileList[i]);
						service.deleteFileList(data); //deleteFileList는 Map타입의 인자값을 받도록 설정
						
					}
				}	
			}
			
			if(!multipartFile.isEmpty()) {//파일 등록 및 수정 시 파일 업로드
				File targetFile = null; 
				Map<String,Object> fileInfo = null;		
				
				for(MultipartFile mpr:multipartFile) {
					
					String org_filename = mpr.getOriginalFilename();	
					String org_fileExtension = org_filename.substring(org_filename.lastIndexOf("."));	
					String stored_filename = UUID.randomUUID().toString().replaceAll("-", "") + org_fileExtension;	
					long filesize = mpr.getSize() ;

					targetFile = new File(path + stored_filename);
					mpr.transferTo(targetFile);
					
					fileInfo = new HashMap<>();
					fileInfo.put("org_filename",org_filename);
					fileInfo.put("stored_filename", stored_filename);
					fileInfo.put("filesize", filesize);
					fileInfo.put("seqno", seqno);
					fileInfo.put("userid", board.getUserid());
					fileInfo.put("kind", kind);
					service.fileInfoRegistry(fileInfo);
		
				}
			}			
			return "{\"message\":\"good\"}";
	}
	
	//공지사항 내용 상세 보기 
	@GetMapping("/customerCenter/noticeView")
	public void getView(@RequestParam("seqno") int seqno, @RequestParam("page") int pageNum,
			@RequestParam(name="keyword",required=false) String keyword,
			Model model,HttpSession session) throws Exception {
		
		String SessionUserid = (String)session.getAttribute("userid");
		BoardVO view = service.view(seqno);
		
		//mapper.hitno(board);
		
		//조회수 증가 처리
		if(!SessionUserid.equals(view.getUserid()))
			service.hitno(view);
		
		//좋아요, 싫어요 처리
		LikeVO likeCheckView = service.likeCheckView(seqno, SessionUserid);
		
		//초기에 좋아요/싫어요 등록이 안되어져 있을 경우 "N"으로 초기화		
		if(likeCheckView == null) {
			model.addAttribute("myLikeCheck", "N");
			model.addAttribute("myDislikeCheck", "N");
		} else if(likeCheckView != null) {
			model.addAttribute("myLikeCheck", likeCheckView.getMylikecheck());
			model.addAttribute("myDislikeCheck", likeCheckView.getMydislikecheck());
		}		
		
		model.addAttribute("view", view);
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);
		model.addAttribute("pre_seqno", service.pre_seqno(seqno, keyword));
		model.addAttribute("next_seqno", service.next_seqno(seqno, keyword));
		model.addAttribute("likeCheckView", likeCheckView);
		model.addAttribute("fileListView", service.fileListView(seqno));
		
	}
	
	// 공지사항 수정 화면
	@GetMapping("/customerCenter/noticeModify")
	public void getModify(@RequestParam("seqno") int seqno, @RequestParam("page") int pageNum, Model model,
			@RequestParam(name="keyword",required=false) String keyword
			) throws Exception{ 
		
		//model.addAttribute("view", mapper.view(seqno));
		
		System.out.println("keyword=" + keyword);
		model.addAttribute("view", service.view(seqno));
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);	
		model.addAttribute("fileListView", service.fileListView(seqno));
		
	}
	
	// 공지사항 수정
	@ResponseBody
	@PostMapping("/customerCenter/noticeModify")
	public String postModify(BoardVO board,@RequestParam("page") int pageNum,
			@RequestParam(name="keyword", required=false) String keyword,
			@RequestParam(name="deleteFileList", required=false) int[] deleteFileList) throws Exception {
	
		service.modify(board);
		
		if(deleteFileList != null) {
			
			for(int i=0; i<deleteFileList.length; i++) {

				//파일 정보 삭제
				FileVO fileInfo = new FileVO();
				fileInfo = service.fileInfo(deleteFileList[i]);
				//File file = new File(path + fileInfo.getStored_filename());
				//file.delete();
				
				//파일 테이블에서 파일 정보 삭제
				Map<String,Object> data = new HashMap<>();
				data.put("kind", "F");
				data.put("fileseqno", deleteFileList[i]);
				service.deleteFileList(data);
				
			}
		}
		
		return "{\"message\":\"good\"}";
	}
	
	// 공지사항 삭제
	@GetMapping("/customerCenter/noticeDelete")
	public String getDelete(@RequestParam("seqno") int seqno) throws Exception {
		Map<String,Object> data = new HashMap<>();
		data.put("kind", "B"); //게시물 삭제시 tbl_file내의 파일 정보 수정
		data.put("seqno", seqno);
		service.deleteFileList(data);
		service.delete(seqno);
		return "redirect:/customerCenter/notice?page=1";
	}
	
}
