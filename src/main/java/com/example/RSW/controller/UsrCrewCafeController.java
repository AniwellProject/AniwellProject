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
		WalkCrew crew = walkCrewService.getCrewById(crewId);

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

		return "usr/walkCrew/cafeHome";
	}

	// ✅ 내가 가입한 크루의 카페로 이동
	@GetMapping("/myCrewCafe")
	public String goToMyCrewCafe(HttpServletRequest req, Model model) {
		Rq rq = (Rq) req.getAttribute("rq");
		int memberId = rq.getLoginedMemberId();

		WalkCrew myCrew = walkCrewService.getCrewByLeaderId(memberId);
		if (myCrew == null) {
			myCrew = walkCrewMemberService.getMyCrew(memberId);
		}

		if (myCrew == null) {
			return rq.historyBackOnView("가입된 크루가 없습니다.");
		}

		// ✅ 이렇게 수정!
		model.addAttribute("crew", myCrew);
		List<Article> articles = articleService.getArticlesByCrewId(myCrew.getId());
		model.addAttribute("articles", articles);

		return "redirect:/usr/crewCafe/cafeHome?crewId=" + myCrew.getId(); // ✅ 요거만 바꾸면 됨
	}

}
