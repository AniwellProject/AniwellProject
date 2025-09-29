package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.WalkCrewMember;

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

	// 멤버리스트 조회
	List<WalkCrewMember> findMembersByCrewId(int crewId);

	// 크루멤버 id 전체 리스트 조회
	List<Integer> intFindMembersByCrewId(int crewId);

	int updateRole(@Param("memberId") int memberId, @Param("crewId") int crewId, @Param("role") String role);

	// 특정 크루에서 해당 멤버의 역할(role)을 조회합니다.
	String findRoleByMemberIdAndCrewId(@Param("memberId") int memberId, @Param("crewId") int crewId);

	// 특정 크루에서 해당 멤버의 신청 상태(status)를 조회합니다.
	String findStatusByMemberIdAndCrewId(@Param("crewId") int crewId, @Param("memberId") int memberId);

	int countPendingRequest(int crewId, int memberId);

	void approveMember(int crewId, int memberId);

	boolean exists(int crewId, int memberId);

	void insert(WalkCrewMember leader);

	void setPendingStatus(int id, int crewId, int memberId);

	// 크루 신청 취소하기
	int cancelJoin(@Param("crewId") int crewId, @Param("memberId") int memberId);

	// WalkCrewMemberRepository.java
	List<WalkCrew> findCrewsByMemberId(@Param("memberId") int memberId);

}