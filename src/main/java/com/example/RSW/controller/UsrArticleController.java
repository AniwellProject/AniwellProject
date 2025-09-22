package com.example.RSW.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.RSW.service.*;
import com.example.RSW.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.interceptor.BeforeActionInterceptor;
import com.example.RSW.util.Ut;

import jakarta.servlet.http.HttpServletRequest;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

@Controller
public class UsrArticleController {

	private final BeforeActionInterceptor beforeActionInterceptor;

	@Autowired
	private Rq rq;

	@Autowired
	private ArticleService articleService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private ReactionPointService reactionPointService;

	@Autowired
	private ReplyService replyService;

	@Autowired
	private Cloudinary cloudinary;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private SpringResourceTemplateResolver springResourceTemplateResolver;
	@Autowired
	private WalkCrewMemberService walkCrewMemberService;

	UsrArticleController(BeforeActionInterceptor beforeActionInterceptor) {
		this.beforeActionInterceptor = beforeActionInterceptor;
	}

	@GetMapping("/usr/article/write/check")
	public ResultData checkWritePermission(HttpServletRequest req, @RequestParam(required = false) Integer boardId,
			@RequestParam(required = false) Integer crewId, @RequestParam(required = false) String type) {

		Rq rq = (Rq) req.getAttribute("rq");

		int loginedMemberId = rq.getLoginedMemberId(); // ✅ 이거 선언 꼭 필요

		// ✅ 크루 관련 권한 체크는 여기서도 반드시 수행
		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			if (crew == null) {
				return ResultData.from("F-1", "존재하지 않는 크루입니다.");
			}

			boolean isApproved = walkCrewService.isApprovedMember(crewId, loginedMemberId);
			if (!isApproved) {
				return ResultData.from("F-2", "승인된 크루 멤버만 글쓰기 가능합니다.");
			}

			if (boardId != null && boardId == 1) {
				boolean isLeader = walkCrewService.isCrewLeader(crewId, loginedMemberId);
				if (!isLeader) {
					return ResultData.from("F-3", "공지사항은 크루장만 작성할 수 있습니다.");
				}
			}

			return ResultData.from("S-1", "글쓰기 권한 확인 성공",
					Map.of("crewId", crewId, "boardId", boardId, "type", type, "crewName", crew.getTitle()));
		}

		// 일반 게시판인 경우 기본 boardId 할당
		if (boardId == null)

		{
			boardId = 2;
		}

		return ResultData.from("S-2", "일반 게시판 글쓰기 가능", Map.of("boardId", boardId, "type", type));
	}

	@PostMapping("/usr/article/doWrite")
	@ResponseBody
	public ResultData doWrite(HttpServletRequest req, @RequestParam(required = false) Integer crewId,
			@RequestParam(required = false) Integer boardId, @RequestParam String title, @RequestParam String body,
			@RequestParam(required = false) MultipartFile imageFile) {

		Rq rq = (Rq) req.getAttribute("rq");
		int loginedMemberId = rq.getLoginedMemberId();

		String imageUrl = null;

		System.out.println("crewId: " + crewId);

		// ✅ 이미지 업로드 처리 (Cloudinary)
		if (imageFile != null && !imageFile.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
				imageUrl = (String) uploadResult.get("secure_url");
				System.out.println("✅ 업로드 성공: " + imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
				return ResultData.from("F-Img", "이미지 업로드 실패");
			}
		}

		// ✅✳️✳️✳️ [여기]에서 크루 권한 검사를 반드시 선행해야 함 ✳️✳️✳️
		if (crewId != null) {
			// ✅ 1. 크루 유효성 검사
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			if (crew == null) {
				return ResultData.from("F-1", "존재하지 않는 크루입니다.");
			}

			// ✅ 2. 승인된 멤버인지 확인
			boolean isApproved = walkCrewService.isApprovedMember(crewId, loginedMemberId);
			if (!isApproved) {
				return ResultData.from("F-2", "승인된 크루 멤버만 글을 작성할 수 있습니다.");
			}

			// ✅ 3. 공지사항이라면 크루장만 가능
			if (boardId != null && boardId == 1) {
				boolean isLeader = walkCrewService.isCrewLeader(crewId, loginedMemberId);
				if (!isLeader) {
					return ResultData.from("F-3", "공지사항은 크루장만 작성할 수 있습니다.");
				}
			}
		}

		// ✅ 게시글 작성 처리
		ResultData rd;
		if (crewId != null) {
			rd = articleService.writeCrewArticle(boardId, crewId, loginedMemberId, title, body, imageUrl);
		} else {
			rd = articleService.writeArticle(loginedMemberId, title, body, String.valueOf(boardId), imageUrl);
		}

		if (rd.isFail()) {
			return ResultData.from(rd.getResultCode(), rd.getMsg());
		}

		// ✅ 생성된 게시글 ID 및 이동 URL 포함 응답
		int articleId = (int) rd.getData1();
		String redirectUrl = crewId != null ? "/usr/article/detail?id=" + articleId + "&crewId=" + crewId
				: "/usr/article/detail?id=" + articleId + "&boardId=" + boardId;

// ✅ 🔔 전체 알림 발송 (공지사항일 때만)
		if (boardId != null && boardId == 1) {

			String link = redirectUrl;

			if (crewId != null) {
				// ✅ 크루공지로 간주
				String notiTitle = "[크루공지] " + title;
				// 기존 전체 전송 대신 크루용으로 커스텀 분기
				notificationService.sendNotificationToMember(notiTitle, link, "CREW_NOTICE", loginedMemberId, crewId);

				// 실제 크루 멤버에게만 보내고 싶으면 위 메서드만 수정
			} else {
				// ✅ 전체 공지
				String notiTitle = "[공지사항] " + title;
				notificationService.sendNotificationToAll(notiTitle, link, "NOTICE", loginedMemberId, crewId);
			}
		}
		return ResultData.from("S-1", "게시글이 성공적으로 작성되었습니다.",
				Map.of("articleId", articleId, "redirectUrl", redirectUrl));
	}

	// ✅ 게시글 수정 처리 (JSON 방식)
	@PostMapping("/usr/article/doModify")
	@ResponseBody
	public ResultData doModify(@RequestParam int id, @RequestParam String title, @RequestParam String body) {

		Article article = articleService.getArticleById(id);
		if (article == null) {
			return ResultData.from("F-1", id + "번 게시글은 존재하지 않습니다.");
		}

		ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);
		if (userCanModifyRd.isFail()) {
			return ResultData.from(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
		}

		articleService.modifyArticle(id, title, body);

		// 클라이언트에 최신 정보 반환
		Article updated = articleService.getArticleById(id);
		return ResultData.from("S-1", "게시글 수정 완료", "data1", updated);
	}

	@ResponseBody
	@PostMapping("/usr/article/doDelete")
	public ResultData doDelete(HttpServletRequest req, @RequestParam int id, @RequestParam int crewId) {
		Rq rq = (Rq) req.getAttribute("rq");
		System.out.println(id + " / " + crewId);
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-0", "로그인 후 이용해주세요.");
		}

		Article article = articleService.getArticleById(id);
		if (article == null) {
			return ResultData.from("F-1", id + "번 게시글은 존재하지 않습니다.");
		}

		ResultData userCanDeleteRd = articleService.userCanDelete(rq.getLoginedMemberId(), article);
		if (userCanDeleteRd.isFail()) {
			return ResultData.from(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg());
		}

		String redirectUrl = article.getCrewId() != null
				? "/usr/article/detail?id=" + id + "&crewId=" + article.getCrewId()
				: "/usr/article/detail?id=" + id + "&boardId=" + article.getBoardId();

		System.out.println("redirectUrl: " + redirectUrl);

		notificationService.deleteByLink(redirectUrl);

		articleService.deleteArticle(id);

		// ✅ 프론트에서 리디렉션할 수 있도록 리턴
		return ResultData.from("S-1", "게시글이 삭제되었습니다.",
				Map.of("redirectUrl", "/usr/crewCafe/cafeHome?crewId=" + crewId));
	}

	@RequestMapping("/usr/article/detail")
	public String showDetail(HttpServletRequest req, HttpServletResponse resp, Model model, int id) throws IOException {
		Rq rq = (Rq) req.getAttribute("rq");
		Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);

		boolean canWriteReply = false;
		int loginMemberId = rq.getLoginedMemberId();

		if (rq.isLogined()) {
			if (article.getCrewId() == null) {
				canWriteReply = true;
			} else {
				List<WalkCrewMember> crewMembers = walkCrewMemberService.getMembersByCrewId(article.getCrewId());
				canWriteReply = crewMembers.stream().anyMatch(cm -> cm.getMemberId() == loginMemberId);
				model.addAttribute("crewMembers", crewMembers); // 필요하면 계속 넘김
			}
		}

		model.addAttribute("canWriteReply", canWriteReply);

		if (article == null) {
			resp.setContentType("text/html; charset=UTF-8");
			PrintWriter out = resp.getWriter();
			out.println("<script>alert('존재하지 않는 게시글입니다.'); history.back();</script>");
			out.flush();
			return null; // 더 이상 진행하지 않음
		}

		// 사용자 리액션 상태 확인 (좋아요/싫어요)
		ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), "article", id);
		if (usersReactionRd.isSuccess()) {
			model.addAttribute("userCanMakeReaction", true);
		}

		// 댓글 조회
		List<Reply> replies = replyService.getForPrintReplies(rq.getLoginedMemberId(), "article", id);
		model.addAttribute("replies", replies);
		model.addAttribute("repliesCount", replies.size());

		model.addAttribute("article", article);
		model.addAttribute("usersReaction", usersReactionRd.getData1());
		model.addAttribute("isAlreadyAddGoodRp",
				reactionPointService.isAlreadyAddGoodRp(rq.getLoginedMemberId(), id, "article"));
		model.addAttribute("isAlreadyAddBadRp",
				reactionPointService.isAlreadyAddBadRp(rq.getLoginedMemberId(), id, "article"));

		return "usr/article/detail"; // 정상 진입 시 detail 페이지 이동
	}

	@GetMapping("/usr/article/list")
	@ResponseBody
	public ResultData showList(HttpServletRequest req, @RequestParam(required = false) Integer boardId,
			@RequestParam(required = false) Integer crewId, @RequestParam(required = false) Integer memberId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "title") String searchKeywordTypeCode,
			@RequestParam(defaultValue = "") String searchKeyword) throws IOException {

		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ crewId, boardId, memberId 모두 있는 경우 → 내가 쓴 글 필터
		if (crewId != null && boardId != null && memberId != null) {
			List<Article> articles = articleService.getArticlesByCrewBoardAndMember(crewId, boardId, memberId);

			return ResultData.from("S-0", "내가 쓴 글 목록 조회 성공",
					Map.of("articles", articles, "crewId", crewId, "boardId", boardId, "memberId", memberId));
		}

		// ✅ crewId와 boardId 모두 존재하는 경우 (크루 게시판)
		if (crewId != null && boardId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			Board board = boardService.getBoardById(boardId);
			if (crew == null || board == null) {
				return ResultData.from("F-1", "존재하지 않는 크루 또는 게시판입니다.");
			}

			List<Article> articles = articleService.getArticlesByCrewIdAndBoardId(crewId, boardId);

			return ResultData.from("S-1", "크루 게시판 글 목록 조회 성공",
					Map.of("crew", crew, "board", board, "articles", articles, "page", page));
		}

		// ✅ crewId만 존재하는 경우 (크루 전체 글)
		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			if (crew == null) {
				return ResultData.from("F-2", "존재하지 않는 크루입니다.");
			}

			List<Article> articles = articleService.getArticlesByCrewId(crewId);

			return ResultData.from("S-2", "크루 전체 글 목록 조회 성공", Map.of("crew", crew, "articles", articles));
		}

		// ✅ 일반 게시판
		if (boardId != null) {
			Board board = boardService.getBoardById(boardId);
			if (board == null) {
				return ResultData.from("F-3", "존재하지 않는 게시판입니다.");
			}

			int itemsInAPage = 10;
			int articlesCount;
			int pagesCount;
			List<Article> articles;

			if (boardId == 1) {
				// 전체 공지사항 (관리자 전용)
				articlesCount = articleService.getAdminOnlyArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
				pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
				articles = articleService.getAdminOnlyArticles(boardId, itemsInAPage * (page - 1), itemsInAPage,
						searchKeywordTypeCode, searchKeyword);
			} else {
				articlesCount = articleService.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
				pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
				articles = articleService.getForPrintArticles(boardId, itemsInAPage * (page - 1), itemsInAPage,
						searchKeywordTypeCode, searchKeyword);
			}

			return ResultData.from("S-3", "게시판 글 목록 조회 성공",
					Map.of("board", board, "articles", articles, "articlesCount", articlesCount, "pagesCount",
							pagesCount, "searchKeywordTypeCode", searchKeywordTypeCode, "searchKeyword", searchKeyword,
							"page", page));
		}

		return ResultData.from("F-4", "boardId 또는 crewId가 필요합니다.");
	}

	@RequestMapping("/usr/article/doIncreaseHitCountRd")
	public ResultData doIncreaseHitCount(int id) {
		ResultData increaseHitCountRd = articleService.increaseHitCount(id);
		if (increaseHitCountRd.isFail()) {
			return increaseHitCountRd;
		}

		return ResultData.newData(increaseHitCountRd, "hitCount", articleService.getArticleHitCount(id));
	}

	// ✅ 모임일정 등록 (JSON 응답)
	@PostMapping("/usr/article/doWriteSchedule")
	@ResponseBody
	public ResultData doWriteSchedule(@RequestParam int crewId, @RequestParam LocalDate scheduleDate,

			@RequestParam String scheduleTitle, @RequestParam(required = false) String scheduleBody,
			HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인이 필요합니다.");
		}
		System.err.print(scheduleDate);
		System.err.printf("%s %s", scheduleTitle, scheduleBody);
		int loginedMemberId = rq.getLoginedMemberId();

		// ✅ 일정 등록
		int scheduleId = articleService.writeSchedule(crewId, loginedMemberId, scheduleDate, scheduleTitle,
				scheduleBody);

		// ✅ ✨ 작성자를 자동으로 참가자로 등록
		articleService.joinSchedule(scheduleId, loginedMemberId);
		
		// ✅ 성공 메시지 리턴 (articleId 없이)
		return ResultData.from("S-1", "모임 일정이 등록되었습니다.",
				Map.of("crewId", crewId, "redirectUrl", "/usr/crewCafe/cafeHome?crewId=" + crewId));
	}

	// ✅ 일정 참가 처리
	@PostMapping("/usr/article/doJoinSchedule")
	@ResponseBody
	public ResultData doJoinSchedule(@RequestParam int scheduleId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		int memberId = rq.getLoginedMemberId();

		if (articleService.isAlreadyJoinedSchedule(scheduleId, memberId)) {
			return ResultData.from("F-2", "이미 참가한 일정입니다.");
		}

		articleService.joinSchedule(scheduleId, memberId);
		return ResultData.from("S-1", "일정 참가 완료");
	}

// 참가자 리스트 조회
	@GetMapping("/usr/article/getParticipants")
	@ResponseBody
	public ResultData getScheduleParticipants(@RequestParam int scheduleId) {
		List<Map<String, Object>> participants = articleService.getScheduleParticipants(scheduleId);
		return ResultData.from("S-1", "참가자 목록", participants);
	}

	// ✅ JSON 응답 방식으로 변경
	@GetMapping("/usr/article/schedule")

	public ResultData showSchedule(@RequestParam int crewId) {
		List<Map<String, Object>> scheduleList = articleService.getSchedulesByCrewId(crewId);

		if (scheduleList == null || scheduleList.isEmpty()) {
			return ResultData.from("F-1", "등록된 모임 일정이 없습니다.");
		}

		return ResultData.from("S-1", "모임 일정 조회 성공", Map.of("crewId", crewId, "schedules", scheduleList));
	}

	@GetMapping("/usr/article/partialList")
	public String showPartialList(Model model) {
		List<Article> articles = articleService.getArticles(); // ✅ 띄어쓰기 제거
		System.out.println("✅ 게시글 수: " + articles.size());
		model.addAttribute("articles", articles);
		return "adm/article/list :: post-list"; // ✅ fragment 이름으로 지정
	}

}