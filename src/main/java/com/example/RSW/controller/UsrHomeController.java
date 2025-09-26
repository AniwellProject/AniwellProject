package com.example.RSW.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.RSW.service.ArticleService;
import com.example.RSW.vo.Article;

@Controller
public class UsrHomeController {

	@Autowired
	private ArticleService articleService;


	@RequestMapping("/usr/home/main")
	public String showMain(Model model) {
		int noticeBoardId = 1; // 공지사항 게시판 ID
		int limit = 5;

		// ✅ 일반 공지사항만 crewId IS NULL인 것만 조회
		List<Article> noticeArticles = articleService.getNoticeArticlesByBoardId(noticeBoardId, limit);
		model.addAttribute("noticeArticles", noticeArticles);

		return "usr/home/main";
	}

	@RequestMapping("/")
	public String showMain2() {
		return "redirect:/usr/home/main";
	}

}