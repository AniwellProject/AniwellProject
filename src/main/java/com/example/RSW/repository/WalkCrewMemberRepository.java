package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.WalkCrew;

@Mapper
public interface WalkCrewMemberRepository {

	// ✅ 크루 참가 요청 (신청 insert)
	void requestToJoinCrew(@Param("crewId") int crewId, @Param("memberId") int memberId);

	// ✅ 내가 가입한 크루 1개 가져오기 (카페 진입용)
	WalkCrew findMyCrewByMemberId(@Param("memberId") int memberId);

	// ✅ 내가 신청한 크루 리스트 조회
	List<WalkCrew> findCrewsAppliedBy(@Param("memberId") int memberId);

	// ✅ 내가 리더인 크루에 신청한 사람 목록
	List<Map<String, Object>> findRequestListByLeaderId(@Param("leaderId") int leaderId);

	// ✅ 이미 가입했는지 확인하는 쿼리
	int countByMemberIdAndCrewId(@Param("memberId") int memberId, @Param("crewId") int crewId);

	int countApprovedMember(int crewId, int memberId);

	// 강퇴 & 탈퇴
	int deleteMemberFromCrew(int crewId, int memberId);
}