package com.example.RSW.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.ArticleRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.WalkCrew;

@Service
public class ArticleService {

	@Autowired
	private ArticleRepository articleRepository;

	public ArticleService(ArticleRepository articleRepository) {
		this.articleRepository = articleRepository;
	}

	public ResultData writeArticle(int loginedMemberId, String title, String body, String boardId, String imageUrl) {
		Map<String, Object> param = new HashMap<>();
		param.put("memberId", loginedMemberId); // 사용자 ID
		param.put("title", title); // 제목
		param.put("body", body); // 본문
		param.put("boardId", boardId); // 게시판 ID
		param.put("imageUrl", imageUrl); // 이미지 URL

		// ✅ Mapper에 Map 전달
		articleRepository.writeArticle(param);

		// ✅ auto_increment ID 가져오기 (useGeneratedKeys, keyProperty="id" 필요)
		Object idObj = param.get("id");
		int id = ((Number) idObj).intValue();

		return ResultData.from("S-1", "게시글이 성공적으로 작성되었습니다.", id);
	}

	public void deleteArticle(int id) {
		articleRepository.deleteArticle(id);
	}

	public void modifyArticle(int id, String title, String body) {
		articleRepository.modifyArticle(id, title, body);
	}

	public ResultData userCanModify(int loginedMemberId, Article article) {

		if (article.getMemberId() != loginedMemberId) {
			return ResultData.from("F-A", Ut.f("%d번 게시글에 대한 수정 권한 없음", article.getId()));
		}

		return ResultData.from("S-1", Ut.f("%d번 게시글 수정 가능", article.getId()));
	}

	public ResultData userCanDelete(int loginedMemberId, Article article) {
		if (article.getMemberId() != loginedMemberId) {
			return ResultData.from("F-A", Ut.f("%d번 게시글에 대한 삭제 권한 없음", article.getId()));
		}

		return ResultData.from("S-1", Ut.f("%d번 게시글 삭제 가능", article.getId()));
	}

	public Article getForPrintArticle(int loginedMemberId, int id) {

		Article article = articleRepository.getForPrintArticle(id);

		controlForPrintData(loginedMemberId, article);

		return article;
	}

	private void controlForPrintData(int loginedMemberId, Article article) {
		if (article == null) {
			return;
		}

		ResultData userCanModifyRd = userCanModify(loginedMemberId, article);
		article.setUserCanModify(userCanModifyRd.isSuccess());

		ResultData userDeleteRd = userCanDelete(loginedMemberId, article);
		article.setUserCanDelete(userDeleteRd.isSuccess());
	}

	public Article getArticleById(int id) {
		return articleRepository.getArticleById(id);
	}

	public List<Article> getArticles() {
		return articleRepository.getArticles();
	}

	public List<Article> getForPrintArticles(int boardId, int itemsInAPage, int page, String searchKeywordTypeCode,
			String searchKeyword) {
		// SELECT * FROM article WHERE boardId = 1 ORDER BY id DESC LIMIT 0, 10;
		// --> 1page
		// SELECT * FROM article WHERE boardId = 1 ORDER BY id DESC LIMIT 10, 10;
		// --> 2page

		int limitFrom = (page - 1) * itemsInAPage;
		int limitTake = itemsInAPage;

		return articleRepository.getForPrintArticles(boardId, limitFrom, limitTake, searchKeywordTypeCode,
				searchKeyword);
	}

	public int getArticleCount(int boardId, String searchKeywordTypeCode, String searchKeyword) {
		return articleRepository.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
	}

	public ResultData increaseHitCount(int id) {
		int affectedRow = articleRepository.increaseHitCount(id);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "해당 게시글 없음", "id", id);
		}

		return ResultData.from("S-1", "조회수 증가", "id", id);
	}

	public Object getArticleHitCount(int id) {
		return articleRepository.getArticleHitCount(id);
	}

	public ResultData increaseGoodReactionPoint(int relId) {
		int affectedRow = articleRepository.increaseGoodReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "좋아요 증가", "affectedRow", affectedRow);
	}

	public ResultData increaseBadReactionPoint(int relId) {
		int affectedRow = articleRepository.increaseBadReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "싫어요 증가", "affectedRow", affectedRow);
	}

	public ResultData decreaseGoodReactionPoint(int relId) {
		int affectedRow = articleRepository.decreaseGoodReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "좋아요 감소", "affectedRow", affectedRow);
	}

	public ResultData decreaseBadReactionPoint(int relId) {
		int affectedRow = articleRepository.decreaseBadReactionPoint(relId);

		if (affectedRow == 0) {
			return ResultData.from("F-1", "없는 게시물");
		}

		return ResultData.from("S-1", "싫어요 감소", "affectedRow", affectedRow);
	}

	public int getGoodRP(int relId) {
		return articleRepository.getGoodRP(relId);
	}

	public int getBadRP(int relId) {
		return articleRepository.getBadRP(relId);
	}

	// 크루전용 articleService
	public List<Article> getArticlesByCrewId(int crewId) {
		return articleRepository.findByCrewId(crewId);
	}

	public ResultData writeCrewArticle(Integer boardId, int crewId, int loginedMemberId, String title, String body,
			String imageUrl) {
		articleRepository.insertCrewArticle(boardId, crewId, loginedMemberId, title, body, imageUrl);

		int id = articleRepository.getLastInsertId();
		return ResultData.from("S-1", "작성 완료", "id", id);
	}

	public List<Article> getRecentArticlesByCrewAndType(int crewId, String type, int limit) {
		return articleRepository.findRecentArticlesByCrewAndType(crewId, type, limit);
	}

	public List<Article> getRecentArticlesByCrewAndBoardId(int crewId, int boardId, int limit) {
		return articleRepository.getRecentArticlesByCrewAndBoardId(crewId, boardId, limit);
	}

	public List<Article> getArticlesByCrewIdAndBoardId(Integer crewId, Integer boardId) {
		return articleRepository.getArticlesByCrewIdAndBoardId(crewId, boardId);
	}

	// 메인홈 / 까페 공지사항 구분하기
	public List<Article> getNoticeArticlesByBoardId(int boardId, int limit) {
		return articleRepository.getNoticeArticlesByBoardId(boardId, limit);
	}

	// ✅ 일정등록 후 생성된 scheduleId 반환하도록 수정
	public int writeSchedule(int crewId, int loginedMemberId, LocalDate scheduleDate, String scheduleTitle,
			String scheduleBody) {

		Article article = Article.builder().crewId(crewId).memberId(loginedMemberId)
				.scheduleDate(java.sql.Date.valueOf(scheduleDate)) // ← ⚠️ 타입 변환 필요할 수 있음
				.title(scheduleTitle).body(scheduleBody).boardId(5) // 일정용 boardId 고정
				.build();

		articleRepository.writeSchedule(article); // insert 실행 (id 자동 주입)

		return article.getId(); // 주입된 PK 반환
	}

	// 공지사항 구분하기 (일반 공지사항 / 크루까페 공지사항)
	public int getAdminOnlyArticleCount(Integer boardId, String searchKeywordTypeCode, String searchKeyword) {
		return articleRepository.getAdminOnlyArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
	}

	public List<Article> getAdminOnlyArticles(Integer boardId, int limitStart, int itemsInAPage,
			String searchKeywordTypeCode, String searchKeyword) {
		return articleRepository.getAdminOnlyArticles(boardId, limitStart, itemsInAPage, searchKeywordTypeCode,
				searchKeyword);
	}

	// 내가쓴글 조회하기
	public List<Article> getArticlesByCrewBoardAndMember(int crewId, int boardId, int memberId) {
		return articleRepository.getArticlesByCrewBoardAndMember(crewId, boardId, memberId);
	}

	// 모임일정리스트불러오기
	public List<Map<String, Object>> getSchedulesByCrewId(int crewId) {
		return articleRepository.getSchedulesByCrewId(crewId);
	}

	// 모임일정 조회
	public void joinSchedule(int scheduleId, int memberId) {
		articleRepository.insertScheduleParticipant(scheduleId, memberId);
	}

	// ✅ 일정 참가자 목록 조회
	public List<Map<String, Object>> getScheduleParticipants(int scheduleId) {
		return articleRepository.getScheduleParticipants(scheduleId);
	}

	public boolean isAlreadyJoinedSchedule(int scheduleId, int memberId) {
		return articleRepository.countScheduleParticipant(scheduleId, memberId) > 0;
	}

}