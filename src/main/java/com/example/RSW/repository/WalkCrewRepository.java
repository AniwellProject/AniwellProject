package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.WalkCrew;

@Mapper
public interface WalkCrewRepository {

	void insert(WalkCrew walkCrew); // 크루 등록

	WalkCrew findById(@Param("id") int id); // ID로 단일 크루 조회

	List<WalkCrew> findByArea(@Param("area") String area); // 지역별 크루 목록 조회

	List<WalkCrew> findAll(); // 전체 크루 목록 조회

	void update(WalkCrew walkCrew); // 크루 정보 수정

	void deleteById(@Param("id") int id); // 크루 삭제

	void insertMemberToCrew(int memberId, int crewId); // 크루 참가

	List<WalkCrew> getWalkCrews(@Param("memberId") int memberId);

	int countByCrewIdAndMemberId(int crewId, int memberId);

	void insertCrewMember(int crewId, int memberId);

	List<Map<String, Object>> getApplicantsByCrewId(@Param("crewId") int crewId);

	Map<String, Object> getApplicantDetail(@Param("crewId") int crewId, @Param("memberId") int memberId);

	int isApprovedMember(int crewId, int memberId);

	void updateMemberStatusToApproved(@Param("crewId") int crewId, @Param("memberId") int memberId);

	WalkCrew findByLeaderId(int leaderId);

	WalkCrew getCrewByMemberId(int memberId);

	List<WalkCrew> findPagedFilteredCrews(String searchKeywordTypeCode, String searchKeyword, String dong, int offset,
			int pageSize);

	int countFilteredCrews(String searchKeywordTypeCode, String searchKeyword, String dong);

	void updateCrewLeader(int crewId, int newLeaderId);

	int updateDescriptionById(int crewId, String newDescription);

	// WalkCrewRepository.java
	List<WalkCrew> getCrewsByLeaderId(@Param("memberId") int memberId);

	// ✅ 내가 '리더'로 있는 크루 목록 조회
	// - walk_crew 테이블의 leaderId 기준
	// - 즉, 내가 만든/운영하는 크루만 조회됨
	List<WalkCrew> findCrewsByLeaderId(int leaderId);

	// ✅ 내가 '승인된 멤버'로 참가한 크루 목록 조회
	// - walk_crew_member 테이블 기준 (status = 'APPROVED')
	// - 내가 만든 크루가 아닌, 단순히 참가한 크루도 포함됨
	List<WalkCrew> findCrewsByMemberId(@Param("memberId") int memberId);

	List<WalkCrew> findJoinedCrewsByMemberId(int memberId);

}