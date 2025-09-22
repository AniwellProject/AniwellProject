package com.example.RSW.controller;

import com.example.RSW.service.ArticleService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.NotificationService;
import com.example.RSW.service.QnaService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/adm/article") // 관리자용 게시글 관련 URL을 처리하는 컨트롤러
public class AdmArticleController {

    @Autowired
    private ArticleService articleService;// 게시글 관련 서비스 의존성 주입

    @Autowired
    private QnaService qnaService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private NotificationService notificationService;

    // 게시글 리스트 페이지 요청 처리
    @RequestMapping("/list")
    public String showList(HttpServletRequest req, Model model,
                           @RequestParam(defaultValue = "1") int page, // 현재 페이지 번호 (기본값 1)
                           @RequestParam(defaultValue = "title") String searchKeywordTypeCode, // 검색 타입 (기본값: 제목)
                           @RequestParam(defaultValue = "") String searchKeyword,
                           @RequestParam(defaultValue = "") String searchType) throws IOException { // 검색 키워드 (기본값: 없음)

        Rq rq = (Rq) req.getAttribute("rq"); // 로그인 정보 등 사용자 정보 객체 획득

        Member loginedMember = rq.getLoginedMember();
        if (loginedMember == null || loginedMember.getAuthLevel() != 7) {
            return "redirect:/";
        }

        int itemsInAPage = 10; // 한 페이지에 보여줄 게시글 수
        int articlesCount = articleService.getArticleCount(0, searchKeywordTypeCode, searchKeyword);
        // boardId = 0은 전체 게시판을 의미하며, 조건에 맞는 게시글 수를 가져옴

        int pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
        // 전체 페이지 수 계산 (게시글 수 ÷ 페이지당 항목 수)

        List<Article> articles = articleService.getForPrintArticles(0, itemsInAPage, page, searchKeywordTypeCode, searchKeyword);
        // 해당 조건에 맞는 게시글 리스트 조회

        List<Member> members = memberService.getForPrintMembers(searchType, searchKeyword);
        model.addAttribute("members", members);
        // 모델에 데이터 전달
        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("articlesCount", articlesCount);
        model.addAttribute("searchKeywordTypeCode", searchKeywordTypeCode);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("articles", articles);
        model.addAttribute("page", page);
        model.addAttribute("qnaList", qnaService.findAll()); // 모든 QnA 조회

        return "adm/article/list"; // JSP 뷰 경로 반환
    }

    // 게시글 삭제 처리 (AJAX 호출 예상)
    @PostMapping("/doDelete")
    @ResponseBody
    public Map<String, Object> doDelete(HttpServletRequest req, int id) {
        Rq rq = (Rq) req.getAttribute("rq"); // 로그인 정보 객체

        Article article = articleService.getArticleById(id);

        if (article == null) {
            return Map.of(
                    "resultCode", "F-1",
                    "msg", id + "번 게시글은 존재하지 않습니다."
            );
        }

        String redirectUrl = article.getCrewId() != null ? "/usr/article/detail?id=" + id + "&crewId=" + article.getCrewId()
                : "/usr/article/detail?id=" + id + "&boardId=" + article.getBoardId();

        System.out.println("redirectUrl: " + redirectUrl);

        notificationService.deleteByLink(redirectUrl);

        Member loginedMember = rq.getLoginedMember();

        if (loginedMember == null || loginedMember.getAuthLevel() != 7) {
            return Map.of(
                    "resultCode", "F-2",
                    "msg", "관리자만 게시글 삭제가 가능합니다."
            );
        }

        articleService.deleteArticle(id);

        return Map.of(
                "resultCode", "S-1",
                "msg", id + "번 게시글이 삭제되었습니다.",
                "redirectUrl", "/adm/article/list"
        );
    }

}