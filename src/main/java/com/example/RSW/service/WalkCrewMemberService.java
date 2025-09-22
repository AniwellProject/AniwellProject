package com.example.RSW.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.BoardRepository;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.repository.WalkCrewMemberRepository;
import com.example.RSW.repository.WalkCrewRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.WalkCrewMember;

@Service
public class WalkCrewMemberService {

	@Autowired
	WalkCrewMemberRepository walkCrewMemberRepository;

	@Autowired
	WalkCrewService walkCrewService;

	// ✅ 크루 참가 요청 (중복 방지 추가)
	public ResultData requestToJoinCrew(int crewId, int memberId) {

		// ✅ 1. 이미 신청한 경우 중복 방지
		boolean alreadyRequested = walkCrewMemberRepository.exists(crewId, memberId);
		if (alreadyRequested) {
			return ResultData.from("F-1", "이미 신청한 크루입니다.");
		}

		// ✅ 2. 신청 정보 DB에 등록
		walkCrewMemberRepository.requestToJoinCrew(crewId, memberId);

		return ResultData.from("S-1", "크루 신청이 완료되었습니다.");
	}

	// ✅ 내가 가입한 크루 1개 가져오기 (크루 카페 진입용)
	public WalkCrew getMyCrew(int memberId) {
		return walkCrewMemberRepository.findMyCrewByMemberId(memberId);
	}

	// ✅ 내가 신청한 크루 리스트
	public List<WalkCrew> getCrewsAppliedBy(int memberId) {
		return walkCrewMemberRepository.findCrewsAppliedBy(memberId);
	}

	// ✅ 내가 리더인 크루에 대한 신청자 리스트
	public List<Map<String, Object>> getRequestListForLeader(int leaderId) {
		return walkCrewMemberRepository.findRequestListByLeaderId(leaderId);
	}

	// ✅ 강퇴 & 탈퇴 공통 처리
	public boolean expelMemberFromCrew(int crewId, int memberId) {
		int affectedRows = walkCrewMemberRepository.deleteMemberFromCrew(crewId, memberId);
		return affectedRows > 0;
	}

	// 크루멤버리스트
	public List<WalkCrewMember> getMembersByCrewId(int crewId) {
		return walkCrewMemberRepository.findMembersByCrewId(crewId);
	}

	public boolean transferLeadership(int crewId, int currentLeaderId, int newLeaderId) {
		try {
			// 1. walk_crew 테이블의 leaderId 변경
			walkCrewService.updateLeader(crewId, newLeaderId);

			// 2. 기존 리더 → subleader로 변경
			int oldUpdate = walkCrewMemberRepository.updateRole(currentLeaderId, crewId, "subleader");

			// 3. 새로운 리더 → leader로 변경
			int newUpdate = walkCrewMemberRepository.updateRole(newLeaderId, crewId, "leader");

			// ✅ 둘 다 업데이트 성공해야 true 반환
			return oldUpdate == 1 && newUpdate == 1;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getRole(int memberId, int crewId) {
		return walkCrewMemberRepository.findRoleByMemberIdAndCrewId(memberId, crewId);
	}

	public boolean isMemberOfCrew(int memberId, int crewId) {
		return walkCrewMemberRepository.countByMemberIdAndCrewId(memberId, crewId) > 0;
	}

	public boolean isApprovedMember(int crewId, int memberId) {
		return walkCrewMemberRepository.countApprovedMember(crewId, memberId) > 0;
	}

	// ① 크루 가입 여부 확인
	public boolean isJoinedCrew(int crewId, int memberId) {
		String role = walkCrewMemberRepository.findRoleByMemberIdAndCrewId(memberId, crewId);
		return role != null && (role.equals("leader") || role.equals("subleader") || role.equals("member")
				|| role.equals("pending"));
	}

	// ② 신청 대기 여부 확인
	public boolean isPendingRequest(int crewId, int memberId) {
		String status = walkCrewMemberRepository.findRoleByMemberIdAndCrewId(memberId, crewId); // status 가져옴
		return "pending".equalsIgnoreCase(status); // status와 비교
	}

	public void approveMember(int crewId, int memberId) {
		walkCrewMemberRepository.approveMember(crewId, memberId);
	}

	// ✅ 크루 멤버 상태를 PENDING으로 되돌리는 서비스 메서드
	public void revertToPendingStatus(int id, int crewId, int memberId) {
		walkCrewMemberRepository.setPendingStatus(id, crewId, memberId);
	}

	// 크루까페 신청취소
	public boolean cancelJoin(int crewId, int memberId) {
		// role을 'pending' → null 처리 또는 신청 row 삭제
		return walkCrewMemberRepository.cancelJoin(crewId, memberId) > 0;
	}

	public boolean isCrewLeader(int crewId, int memberId) {
		String role = walkCrewMemberRepository.findRoleByMemberIdAndCrewId(memberId, crewId);
		return "leader".equalsIgnoreCase(role); // 대소문자 무시 비교
	}

	public boolean isPending(int crewId, int memberId) {
		String status = walkCrewMemberRepository.findStatusByMemberIdAndCrewId(crewId, memberId);
		return "pending".equalsIgnoreCase(status); // 실제 DB 저장 상태값과 맞춰야 함
	}

	public List<WalkCrew> getCrewsByMemberId(int memberId) {
		return walkCrewMemberRepository.findCrewsByMemberId(memberId);
	}

}