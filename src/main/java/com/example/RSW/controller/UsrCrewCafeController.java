package com.example.RSW.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Article;
import com.example.RSW.util.Ut;
import com.example.RSW.config.AppConfig;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.service.ArticleService;
import com.example.RSW.service.DistrictService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.WalkCrewMemberService;
import com.example.RSW.service.WalkCrewService;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequestMapping("/usr/crewCafe")
@Controller
public class UsrCrewCafeController {

	@Autowired
	private ArticleService articleService;

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private WalkCrewMemberService walkCrewMemberService;

	@Autowired
	private Cloudinary cloudinary;

	@GetMapping("")
	public ResultData index(@RequestParam(required = false) Integer crewId) {
		if (crewId == null) {
			return ResultData.from("F-1", "crewId가 필요합니다.");
		}
		return ResultData.from("S-1", "크루 홈으로 이동 가능", "crewId", crewId);
	}

	// 까페홈에 article 글 보이게 하기
	@GetMapping("/cafeHome")
	public String showCafeHome(@RequestParam(defaultValue = "0") int crewId, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		int memberId = rq.getLoginedMemberId();
		WalkCrew crew = walkCrewService.getCrewById(crewId);

		// ✅ 가입 여부 / 신청 여부
		boolean isJoined = walkCrewMemberService.isApprovedMember(crewId, memberId);
		boolean isPending = walkCrewMemberService.isPendingRequest(crewId, memberId);
		boolean isLeader = crew.getLeaderId() == memberId; // 리더인지

		// ✅ 게시판 ID 기준으로 불러오기
		int noticeBoardId = 1; // 공지사항
		int freeBoardId = 3; // 자유게시판
		int galleryBoardId = 4; // 사진첩
		int scheduleBoardId = 5; // 일정 게시판

		// 로그용
		System.out.println("✅ crewId = " + crewId);

		// ✅ 공지글 5개
		List<Article> noticeArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, noticeBoardId, 5);
		System.out.println("✅ noticeArticles.size = " + noticeArticles.size());

		// ✅ 자유글 5개
		List<Article> freeArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, freeBoardId, 5);
		System.out.println("✅ freeArticles.size = " + freeArticles.size());
		for (Article a : freeArticles) {
			System.out.println("📝 자유글: id=" + a.getId() + ", title=" + a.getTitle());
		}

		// ✅ 사진용 게시글: 자유게시판(boardId=3) 중 imageUrl이 있는 글만 최대 20개
		List<Article> galleryArticles = articleService
				.getRecentArticlesByCrewAndBoardId(crewId, freeBoardId, 20).stream().filter(a -> a.getImageUrl() != null
						&& !a.getImageUrl().isEmpty() && !"undefined".equals(a.getImageUrl()))
				.collect(Collectors.toList());

		System.out.println("✅ galleryArticles.size = " + galleryArticles.size());

		// 일정모임 리스트 불러오기
		List<Article> scheduleArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, scheduleBoardId, 10);

		// 모델에 데이터 전달
		model.addAttribute("crew", crew);
		model.addAttribute("noticeArticles", noticeArticles);
		model.addAttribute("freeArticles", freeArticles);
		model.addAttribute("galleryArticles", galleryArticles);
		model.addAttribute("scheduleArticles", scheduleArticles);
		model.addAttribute("isJoined", isJoined);
		System.out.println("✅ isPending = " + isPending);
		model.addAttribute("isPending", isPending);
		model.addAttribute("isLeader", isLeader);
		return "usr/walkCrew/cafeHome";
	}

	@GetMapping("/usr/crew/myCrewCafe")
	@ResponseBody
	public ResultData<Map<String, Object>> getMyCrewCafe(HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-AUTH", "로그인 후 이용해주세요.");
		}

		int memberId = rq.getLoginedMemberId();

		
		// ✅ 1. 내가 만든 크루 전체 조회
		List<WalkCrew> myCrews = walkCrewService.getCrewsByLeaderId(memberId);
		if (myCrews == null)
			myCrews = new ArrayList<>();

		// ✅ 2. 내가 리더가 아닌 가입한 크루 전체 조회
		List<WalkCrew> joinedCrews = walkCrewMemberService.getCrewsByMemberId(memberId);
		if (joinedCrews == null)
			joinedCrews = new ArrayList<>();

		// ✅ 3. 대표 크루(myCrew) 하나 선택 (기존 로직 그대로 유지)
		WalkCrew myCrew = null;
		if (myCrews != null && !myCrews.isEmpty()) {
			myCrew = myCrews.get(0); // 내가 만든 크루 중 첫 번째
		} else if (joinedCrews != null && !joinedCrews.isEmpty()) {
			myCrew = joinedCrews.get(0); // 가입한 크루 중 첫 번째
		}

		// ✅ 4. 대표 크루가 하나도 없다면 실패 응답
		if (myCrew == null) {
			return ResultData.from("F-1", "가입된 크루가 없습니다.");
		}

		// ✅ 5. 대표 크루의 게시글 목록 (기존 유지)
		List<Article> articles = articleService.getArticlesByCrewId(myCrew.getId());

		// ✅ 6. 응답 데이터 구성
		Map<String, Object> data = new HashMap<>();
		data.put("crew", myCrew); // ✅ 기존: 대표 크루 1개 (crew)
		data.put("articles", articles); // ✅ 기존: 대표 크루의 게시글
		data.put("myCrews", myCrews); // 🔼 추가: 내가 만든 크루 전체 리스트
		data.put("joinedCrews", joinedCrews); // 🔼 추가: 내가 가입한 크루 전체 리스트

		System.out.println("rq = " + rq);
		System.out.println("memberId = " + memberId);
		System.out.println("myCrews.size = " + myCrews.size());
		System.out.println("joinedCrews.size = " + joinedCrews.size());
		
		return ResultData.from("S-1", "나의 크루와 게시글을 불러왔습니다.", data);
	}

}