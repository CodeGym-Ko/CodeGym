package com.board.controller;

import java.io.File;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.board.dto.BoardVO;
import com.board.dto.FileVO;
import com.board.dto.LikeVO;
import com.board.dto.ReplyVO;
import com.board.service.BoardService;
import com.board.util.Page;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class BoardController {

	@Autowired
	BoardService service; // 의존성 주입
	
	@GetMapping("/")
	public String index(Model model) throws Exception {
		model.addAttribute("todayWorkout", service.hotBoard("todayWorkout"));
		model.addAttribute("freeHotBoard", service.hotBoard("free"));
		return "index";
	}
	
	
	// 게시물 목록 보기
	@GetMapping("/board/list")
	public String getList(@RequestParam("page") int pageNum,
			@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword, Model model,
			HttpSession session) {
			int postNum = 10; // 한 화면에 보여지는 게시물 행의 개수
			int pageListCount = 10; // 화면 하단에 보여지는 페이지 리스트 내의 페이지 개수
			int startPoint = (pageNum - 1) * postNum;
			int endPoint = pageNum * postNum;
			int totalCount = service.getTotalCount(keyword, "free");

			Page page = new Page();
			model.addAttribute("list", service.list(startPoint, endPoint, keyword));
			model.addAttribute("page", pageNum);
			model.addAttribute("keyword", keyword);
			model.addAttribute("pageList", page.getPageList(pageNum, postNum, pageListCount, totalCount, keyword));
			return "/board/list";

	}
	
	// 파일 여부 체크
	@ResponseBody
	@PostMapping("/board/fileCheck")
	public String postFileCheck(@RequestBody int seqno) throws Exception {
		if(service.fileListView(seqno).size() == 0) {
			return "{\"status\":\"bad\"}";
		} else {
			return "{\"status\":\"good\"}";
		}
	}

	// 게시물 작성 화면보기
	@GetMapping("/board/write")
	public String getBoardWrite(Model model, HttpSession session) {
		return "/board/write";
	}

	// 첨부 파일 없는 게시물 등록
	@ResponseBody
	@PostMapping("/board/write")
	public String postWrite(BoardVO board) throws Exception {
		int seqno = service.getSeqnoWithNextval();
		board.setSeqno(seqno);
		board.setBoardType("free");
		service.write(board);
		return "{\"message\":\"good\"}";

	}

	// 파일 업로드
	@ResponseBody
	@PostMapping("/board/fileUpload")
	public String postFileUpload(BoardVO board, @RequestParam("SendToFileList") List<MultipartFile> multipartFile,
			@RequestParam("kind") String kind,
			@RequestParam(name = "deleteFileList", required = false) int[] deleteFileList) throws Exception {

		String path = "c:\\Repository\\file\\";
		int seqno = 0;
		if (kind.equals("I")) { // 게시물 등록
			seqno = service.getSeqnoWithNextval();
			board.setSeqno(seqno);
			board.setBoardType("free");
			service.write(board);
		}
		if (kind.equals("U")) { // 게시물 수정 시 파일 수정
			seqno = board.getSeqno();
			service.modify(board);

			if (deleteFileList != null) {

				for (int i = 0; i < deleteFileList.length; i++) {

					// 파일 삭제
					FileVO fileInfo = new FileVO();
					fileInfo = service.fileInfo(deleteFileList[i]);
//					File file = new File(path + fileInfo.getStored_filename());
//					file.delete();

					// 파일 테이블에서 파일 정보 삭제
					Map<String, Object> data = new HashMap<>();
					data.put("kind", "F"); // 게시물 수정에서 삭제할 파일 목록이 전송되면 이 값을 받아서 tbl_file내에 있는 파일 정보를 하나씩 삭제하는
											// deleteFileList 실행
					data.put("fileseqno", deleteFileList[i]);
					service.deleteFileList(data); // deleteFileList는 Map 타입의 인자값을 받도록 설정

				}
			}
		}

		if (!multipartFile.isEmpty()) { // 파일 등록 및 수정 시 파일 업로드
			File targetFile = null;
			Map<String, Object> fileInfo = null;

			for (MultipartFile mpr : multipartFile) {

				String org_filename = mpr.getOriginalFilename();
				String org_fileExtension = org_filename.substring(org_filename.lastIndexOf("."));
				String stored_filename = UUID.randomUUID().toString().replaceAll("-", "") + org_fileExtension;
				long filesize = mpr.getSize();

				targetFile = new File(path + stored_filename);
				mpr.transferTo(targetFile);

				fileInfo = new HashMap<>();
				fileInfo.put("org_filename", org_filename);
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

	// 게시물 상세보기
	@GetMapping("/board/view")
	public String getView(Model model,
			@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
			@RequestParam("seqno") int seqno, @RequestParam("page") int pageNum, HttpSession session) throws Exception {

		String sessionUserId = (String) session.getAttribute("userid");
		BoardVO board = service.view(seqno);
		if(sessionUserId == null) {
			sessionUserId = "tmp";
		}
		// 조회수 증가처리
		if (!board.getUserid().equals(sessionUserId)) {
			service.hitno(board);
		}

		// 좋아요, 싫어요 처리
		LikeVO likeCheckView = service.likeCheckView(seqno, sessionUserId);
		// 초기에 좋아요, 싫어요 등록이 안되어져 있을 경우 "N"으로 초기화
		if (likeCheckView == null) {
			model.addAttribute("myLikeCheck", "N");
			model.addAttribute("myDislikeCheck", "N");
		} else if (likeCheckView != null) {
			model.addAttribute("myLikeCheck", likeCheckView.getMylikecheck());
			model.addAttribute("myDislikeCheck", likeCheckView.getMydislikecheck());
		}

		model.addAttribute("view", service.view(seqno));
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);
		model.addAttribute("pre_seqno", service.pre_seqno(seqno, keyword));
		model.addAttribute("next_seqno", service.next_seqno(seqno, keyword));
		model.addAttribute("likeCheckView", likeCheckView);
		model.addAttribute("fileListView", service.fileListView(seqno));
		return "/board/view";
	}

	// 좋아요/싫어요 관리
	@ResponseBody
	@PostMapping(value = "/board/likeCheck")
	public Map<String, Object> postLikeCheck(@RequestBody Map<String, Object> likeCheckData) throws Exception {

		int seqno = (int) likeCheckData.get("seqno");
		String userid = (String) likeCheckData.get("userid");
		int checkCnt = (int) likeCheckData.get("checkCnt");

		// 현재 날짜, 시간 구해서 좋아요/싫어요 한 날짜/시간 입력 및 수정
		String likeDate = "";
		String dislikeDate = "";
		if (likeCheckData.get("mylikecheck").equals("Y"))
			likeDate = LocalDateTime.now().toString();
		else if (likeCheckData.get("mydislikecheck").equals("Y"))
			dislikeDate = LocalDateTime.now().toString();

		likeCheckData.put("likedate", likeDate);
		likeCheckData.put("dislikedate", dislikeDate);

		// TBL_LIKE 테이블 입력/수정
		LikeVO likeCheckView = service.likeCheckView(seqno, userid);
		if (likeCheckView == null)
			service.likeCheckRegistry(likeCheckData);
		else
			service.likeCheckUpdate(likeCheckData);

		// TBL_BOARD 내의 likecnt,dislikecnt 입력/수정
		BoardVO board = service.view(seqno);

		int likeCnt = board.getLikecnt();
		int dislikeCnt = board.getDislikecnt();

		switch (checkCnt) {
		case 1:
			likeCnt--;
			break;
		case 2:
			likeCnt++;
			dislikeCnt--;
			break;
		case 3:
			likeCnt++;
			break;
		case 4:
			dislikeCnt--;
			break;
		case 5:
			likeCnt--;
			dislikeCnt++;
			break;
		case 6:
			dislikeCnt++;
			break;
		}

		service.boardLikeUpdate(seqno, likeCnt, dislikeCnt);

		// AJAX에 전달할 map JSON 데이터 만들기
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("seqno", seqno);
		map.put("likeCnt", likeCnt);
		map.put("dislikeCnt", dislikeCnt);
		// map 대신 이렇게 수정 return "{\"username\":\"" +
		// URLEncoder.encode(user.getUsername(), "UTF-8") + "\",\"status\":\"good\"}";
		return map;
	}

	// 게시물 수정 화면 보기
	@GetMapping("/board/modify")
	public String getModify(Model model, @RequestParam("seqno") int seqno, @RequestParam("page") int pageNum,
			@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword, HttpSession session)
			throws Exception {

		model.addAttribute("view", service.view(seqno));
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);
		model.addAttribute("fileListView", service.fileListView(seqno));
		return "/board/modify";
	}

	// 게시물 수정하기
	@ResponseBody
	@PostMapping("/board/modify")
	public String postModify(BoardVO board, @RequestParam("page") int pageNum,
			@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
			@RequestParam(name = "deleteFileList", required = false) int[] deleteFileList) throws Exception {

		service.modify(board);
//		mapper.modify(board);
		if (deleteFileList != null) {

			for (int i = 0; i < deleteFileList.length; i++) {
				// 파일 테이블에서 파일 정보 삭제
				Map<String, Object> data = new HashMap<>();
				data.put("kind", "F"); // 게시물 수정에서 삭제할 파일 목록이 전송되면 이 값을 받아서 tbl_file내에 있는 파일 정보를 하나씩 삭제하는
										// deleteFileList 실행
				data.put("fileseqno", deleteFileList[i]);
				service.deleteFileList(data); // deleteFileList는 Map 타입의 인자값을 받도록 설정
			}
		}
		return "{\"message\":\"good\"}";
	}

	// 게시물 삭제
	@GetMapping("/board/delete")
	public String getDelete(@RequestParam("seqno") int seqno, HttpSession session, Model model) throws Exception {
		Map<String, Object> data = new HashMap<>();
		data.put("kind", "B");
		data.put("seqno", seqno);
		service.deleteFileList(data);
		service.delete(seqno);
		return "redirect:/board/list?page=1";

	}

	// 파일 다운로드
	@GetMapping("/board/fileDownload")
	public void fileDownload(@RequestParam("fileseqno") int fileseqno, HttpServletResponse rs) throws Exception {
		FileVO file = service.fileInfo(fileseqno);
		String path = "c:\\Repository\\file\\";
		byte fileByte[] = FileUtils.readFileToByteArray(new File(path + file.getStored_filename()));
		// 헤드값을 Content-Disposition로 주게 되면 Response Body로 오는 값을 filename으로 다운받으라는 것임
		// 예) Content-Disposition: attachment; filename="hello.jpg"
		rs.setContentType("application/octet-stream");
		rs.setContentLength(fileByte.length);
		rs.setHeader("Content-Disposition",
				"attachment; filename=\"" + URLEncoder.encode(file.getOrg_filename(), "UTF-8") + "\";");
		rs.getOutputStream().write(fileByte);
		rs.getOutputStream().flush();
		rs.getOutputStream().close();

	}

	// 댓글 처리
	@ResponseBody
	@PostMapping("/board/reply")
	public List<ReplyVO> postReply(@RequestBody ReplyVO reply, @RequestParam("option") String option) throws Exception {

		switch (option) {

		case "I":
			service.replyRegistry(reply); // 댓글 등록
			break;
		case "U":
			service.replyUpdate(reply); // 댓글 수정
			break;
		case "D":
			service.replyDelete(reply); // 댓글 삭제
			break;
		}

		return service.replyView(reply);
	}
	

	// 공지사항
	@GetMapping("/customerCenter/notice")
	public String getNotice(@RequestParam("page") int pageNum,
			@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword, Model model)
			throws Exception {
		// 목록 보기
		int postNum = 10; // 한 화면에 보여지는 게시물 행의 갯수
		int pageListCount = 10; // 화면 하단에 보여지는 페이지리스트 내의 페이지 갯수
		int startPoint = (pageNum - 1) * postNum + 1;
		int endPoint = pageNum * postNum;
		int totalCount = service.getTotalCount(keyword, "notice");

		Page page = new Page();
		model.addAttribute("list", service.notice(startPoint, endPoint, keyword));
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);
		model.addAttribute("pageList", page.getPageList(pageNum, postNum, pageListCount, totalCount, keyword));
		return "/customerCenter/notice";
	}
	
	// 공지사항 작성 화면
	@GetMapping("/customerCenter/noticeWrite")
	public void getNoticeWrite() {
		
	}
	
	//첨부 파일 없는 공지사항 등록
	@ResponseBody
	@PostMapping("/customerCenter/noticeWrite")
	public String postNoticeWrite(BoardVO board) throws Exception{

		int seqno = service.getSeqnoWithNextval();
		board.setSeqno(seqno);
		board.setBoardType("notice");
		service.write(board);
		return "{\"message\":\"good\"}";

	}
	
	// 첨부 파일 있는 공지사항 등록
	@ResponseBody
	@PostMapping("/customerCenter/fileUpload")
		public String postNoticeFileUpload(BoardVO board,@RequestParam("SendToFileList") List<MultipartFile> multipartFile, 
				@RequestParam("kind") String kind,@RequestParam(name="deleteFileList", required=false) int[] deleteFileList) throws Exception{

			String path = "c:\\Repository\\file\\"; 
			int seqno = 0;
			
			if(kind.equals("I")) { //게시물 등록 시 게시물 등록 
				seqno = service.getSeqnoWithNextval();
				board.setSeqno(seqno);
				board.setBoardType("notice");
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
	public void getNoticeView(@RequestParam("seqno") int seqno, @RequestParam("page") int pageNum,
			@RequestParam(name="keyword",required=false) String keyword,
			Model model,HttpSession session) throws Exception {
		
		String SessionUserid = (String)session.getAttribute("userid");
		BoardVO view = service.view(seqno);
		
		if(SessionUserid == null) {
			SessionUserid = "tmp";
		}
			
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
	public void getNoticeModify(@RequestParam("seqno") int seqno, @RequestParam("page") int pageNum, Model model,
			@RequestParam(name="keyword",required=false) String keyword
			) throws Exception{ 
		
		//model.addAttribute("view", mapper.view(seqno));
		
		model.addAttribute("view", service.view(seqno));
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);	
		model.addAttribute("fileListView", service.fileListView(seqno));
		
	}
	
	// 공지사항 수정
	@ResponseBody
	@PostMapping("/customerCenter/noticeModify")
	public String postNoticeModify(BoardVO board,@RequestParam("page") int pageNum,
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
	public String getNoticeDelete(@RequestParam("seqno") int seqno) throws Exception {
		Map<String,Object> data = new HashMap<>();
		data.put("kind", "B"); //게시물 삭제시 tbl_file내의 파일 정보 수정
		data.put("seqno", seqno);
		service.deleteFileList(data);
		service.delete(seqno);
		return "redirect:/customerCenter/notice?page=1";
	}
	
	
	// 오운완 게시물 목록 보기
	@GetMapping("/todayWorkout/list")
	public String getTodayWorkout(@RequestParam("page") int pageNum,
			@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword, Model model,
			HttpSession session) {
		int postNum = 10; // 한 화면에 보여지는 게시물 행의 개수
		int pageListCount = 10; // 화면 하단에 보여지는 페이지 리스트 내의 페이지 개수
		int startPoint = (pageNum - 1) * postNum;
		int endPoint = pageNum * postNum;
		int totalCount = service.getTotalCount(keyword, "todayWorkout");
		
		Page page = new Page();
		model.addAttribute("list", service.todayWorkoutList(startPoint, endPoint, keyword));
		model.addAttribute("page", pageNum);
		model.addAttribute("keyword", keyword);
		model.addAttribute("pageList", page.getPageList(pageNum, postNum, pageListCount, totalCount, keyword));
		return "/todayWorkout/list";
		
	}
	
	// 게시물 작성 화면보기
	@GetMapping("/todayWorkout/write")
	public String getTodayWorkoutWrite(Model model, HttpSession session) {
		return "/todayWorkout/write";
	}

	// 첨부 파일 없는 게시물 등록
	@ResponseBody
	@PostMapping("/todayWorkout/write")
	public String postTodayWorkoutWrite(BoardVO board) throws Exception {
		int seqno = service.getSeqnoWithNextval();
		board.setSeqno(seqno);
		board.setBoardType("todayWorkout");
		service.write(board);
		return "{\"message\":\"good\"}";

	}

	// 파일 업로드
	@ResponseBody
	@PostMapping("/todayWorkout/fileUpload")
	public String postTodayWorkoutFileUpload(BoardVO board, @RequestParam("SendToFileList") List<MultipartFile> multipartFile,
			@RequestParam("kind") String kind,
			@RequestParam(name = "deleteFileList", required = false) int[] deleteFileList) throws Exception {

		String path = "c:\\Repository\\file\\";
		int seqno = 0;
		if (kind.equals("I")) { // 게시물 등록
			seqno = service.getSeqnoWithNextval();
			board.setSeqno(seqno);
			board.setBoardType("todayWorkout");
			service.write(board);
		}
		if (kind.equals("U")) { // 게시물 수정 시 파일 수정
			seqno = board.getSeqno();
			service.modify(board);

			if (deleteFileList != null) {

				for (int i = 0; i < deleteFileList.length; i++) {

					// 파일 삭제
					FileVO fileInfo = new FileVO();
					fileInfo = service.fileInfo(deleteFileList[i]);
//						File file = new File(path + fileInfo.getStored_filename());
//						file.delete();

					// 파일 테이블에서 파일 정보 삭제
					Map<String, Object> data = new HashMap<>();
					data.put("kind", "F"); // 게시물 수정에서 삭제할 파일 목록이 전송되면 이 값을 받아서 tbl_file내에 있는 파일 정보를 하나씩 삭제하는
											// deleteFileList 실행
					data.put("fileseqno", deleteFileList[i]);
					service.deleteFileList(data); // deleteFileList는 Map 타입의 인자값을 받도록 설정

				}
			}
		}

		if (!multipartFile.isEmpty()) { // 파일 등록 및 수정 시 파일 업로드
			File targetFile = null;
			Map<String, Object> fileInfo = null;

			for (MultipartFile mpr : multipartFile) {

				String org_filename = mpr.getOriginalFilename();
				String org_fileExtension = org_filename.substring(org_filename.lastIndexOf("."));
				String stored_filename = UUID.randomUUID().toString().replaceAll("-", "") + org_fileExtension;
				long filesize = mpr.getSize();

				targetFile = new File(path + stored_filename);
				mpr.transferTo(targetFile);

				fileInfo = new HashMap<>();
				fileInfo.put("org_filename", org_filename);
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

	// 게시물 상세보기
		@GetMapping("/todayWorkout/view")
		public String getTodayWorkoutView(Model model,
				@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
				@RequestParam("seqno") int seqno, @RequestParam("page") int pageNum, HttpSession session) throws Exception {

			String sessionUserId = (String) session.getAttribute("userid");
			BoardVO board = service.view(seqno);
			if(sessionUserId == null) {
				sessionUserId = "tmp";
			}
			// 조회수 증가처리
			if (!board.getUserid().equals(sessionUserId)) {
				service.hitno(board);
			}

			// 좋아요, 싫어요 처리
			LikeVO likeCheckView = service.likeCheckView(seqno, sessionUserId);
			// 초기에 좋아요, 싫어요 등록이 안되어져 있을 경우 "N"으로 초기화
			if (likeCheckView == null) {
				model.addAttribute("myLikeCheck", "N");
				model.addAttribute("myDislikeCheck", "N");
			} else if (likeCheckView != null) {
				model.addAttribute("myLikeCheck", likeCheckView.getMylikecheck());
				model.addAttribute("myDislikeCheck", likeCheckView.getMydislikecheck());
			}

			model.addAttribute("view", service.view(seqno));
			model.addAttribute("page", pageNum);
			model.addAttribute("keyword", keyword);
			model.addAttribute("pre_seqno", service.pre_seqno(seqno, keyword));
			model.addAttribute("next_seqno", service.next_seqno(seqno, keyword));
			model.addAttribute("likeCheckView", likeCheckView);
			model.addAttribute("fileListView", service.fileListView(seqno));
			return "/todayWorkout/view";
		}
		
		// 게시물 삭제
		@GetMapping("/todayWorkout/delete")
		public String getTodayWorkoutDelete(@RequestParam("seqno") int seqno, HttpSession session, Model model) throws Exception {
			Map<String, Object> data = new HashMap<>();
			data.put("kind", "B");
			data.put("seqno", seqno);
			service.deleteFileList(data);
			service.delete(seqno);
			return "redirect:/todayWorkout/list?page=1";

		}
		
		
		// 게시물 수정 화면 보기
		@GetMapping("/todayWorkout/modify")
		public String getTodayWorkoutModify(Model model, @RequestParam("seqno") int seqno, @RequestParam("page") int pageNum,
				@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword, HttpSession session)
				throws Exception {

			model.addAttribute("view", service.view(seqno));
			model.addAttribute("page", pageNum);
			model.addAttribute("keyword", keyword);
			model.addAttribute("fileListView", service.fileListView(seqno));
			return "/todayWorkout/modify";
		}
		
		// 게시물 수정하기
		@ResponseBody
		@PostMapping("/todayWorkout/modify")
		public String postTodayWorkoutModify(BoardVO board, @RequestParam("page") int pageNum,
				@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
				@RequestParam(name = "deleteFileList", required = false) int[] deleteFileList) throws Exception {

			service.modify(board);
			if (deleteFileList != null) {

				for (int i = 0; i < deleteFileList.length; i++) {
					// 파일 테이블에서 파일 정보 삭제
					Map<String, Object> data = new HashMap<>();
					data.put("kind", "F"); // 게시물 수정에서 삭제할 파일 목록이 전송되면 이 값을 받아서 tbl_file내에 있는 파일 정보를 하나씩 삭제하는
											// deleteFileList 실행
					data.put("fileseqno", deleteFileList[i]);
					service.deleteFileList(data); // deleteFileList는 Map 타입의 인자값을 받도록 설정
				}
			}
			return "{\"message\":\"good\"}";
		}
}
