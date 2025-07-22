package com.example.RSW.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.RSW.vo.Article;

@Mapper
public interface ArticleRepository {

	public int writeArticle(Map<String, Object> param); // ✅ 수정

	public void deleteArticle(int id);

	public void modifyArticle(int id, String title, String body);

	public int getLastInsertId();

	public Article getArticleById(int id);

	public List<Article> getArticles();

	public Article getForPrintArticle(int loginedMemberId);

	public List<Article> getForPrintArticles(int boardId, int limitFrom, int limitTake, String searchKeywordTypeCode,
			String searchKeyword);

	public int getArticleCount(int boardId, String searchKeywordTypeCode, String searchKeyword);

	public int increaseHitCount(int id);

	public int getArticleHitCount(int id);

	public int increaseGoodReactionPoint(int relId);

	public int decreaseGoodReactionPoint(int relId);

	public int increaseBadReactionPoint(int relId);

	public int decreaseBadReactionPoint(int relId);

	public int getGoodRP(int relId);

	public int getBadRP(int relId);

	List<Article> findAll();

	public List<Article> findByCrewId(int crewId);

	// 크루용 리포지터리
	void insertCrewArticle(@Param("boardId") Integer boardId, @Param("crewId") Integer crewId,
			@Param("memberId") int memberId, @Param("title") String title, @Param("body") String body,
			@Param("imageUrl") String imageUrl);

	List<Article> findRecentArticlesByCrewAndType(@Param("crewId") int crewId, @Param("typeCode") String typeCode,
			@Param("limit") int limit);

	public List<Article> getRecentArticlesByCrewAndBoardId(int crewId, int boardId, int limit);

	public List<Article> getArticlesByCrewIdAndBoardId(Integer crewId, Integer boardId);

	// 메인홈 / 까페 공지사항 관련
	List<Article> getNoticeArticlesByBoardId(@Param("boardId") int boardId, @Param("limit") int limit);

	public int getAdminOnlyArticleCount(Integer boardId, String searchKeywordTypeCode, String searchKeyword);

	public List<Article> getAdminOnlyArticles(Integer boardId, int limitStart, int itemsInAPage,
			String searchKeywordTypeCode, String searchKeyword);;

	// 일정등록하기
	public void writeSchedule(@Param("crewId") int crewId, @Param("loginedMemberId") int loginedMemberId,
			@Param("scheduleDate") String scheduleDate, @Param("scheduleTitle") String scheduleTitle,
			String scheduleBody);

	public List<Map<String, Object>> getSchedulesByCrewId(int crewId);

}