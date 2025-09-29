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
public class WalkCrewService {

	@Autowired
	private DistrictRepository districtRepository;

	@Autowired
	private WalkCrewMemberRepository walkCrewMemberRepository;

	private final WalkCrewRepository walkCrewRepository;

	public WalkCrewService(WalkCrewRepository walkCrewRepository) {
		this.walkCrewRepository = walkCrewRepository;
	}

	// 크루 등록
	public void createCrew(WalkCrew walkCrew) {
		// 1. 크루 DB 저장 (자동 ID 생성 포함)
		walkCrewRepository.insert(walkCrew);

		// 2. 크루장을 walk_crew_member 테이블에 등록
		WalkCrewMember leader = new WalkCrewMember();
		leader.setCrewId(walkCrew.getId()); // 크루 PK
		leader.setMemberId(walkCrew.getLeaderId()); // 크루장 ID
		leader.setRole("leader"); // 역할 지정

		walkCrewMemberRepository.insert(leader); // DB에 등록
	}

	// ID로 크루 상세 조회
	public WalkCrew getCrewById(int id) {
		return walkCrewRepository.findById(id);
	}

	// 지역별 크루 목록 조회
	public List<WalkCrew> getCrewsByArea(String area) {
		return walkCrewRepository.findByArea(area);
	}

	// 전체 크루 목록 조회
	public List<WalkCrew> getAllCrews() {
		return walkCrewRepository.findAll();
	}

	// 크루 수정
	public void updateCrew(WalkCrew walkCrew) {
		walkCrewRepository.update(walkCrew);
	}

	// 크루 삭제
	public void deleteCrew(int id) {
		walkCrewRepository.deleteById(id);
	}

	public void joinCrew(int memberId, int crewId) {
		if (!hasAlreadyJoined(crewId, memberId)) {
			addMemberToCrew(crewId, memberId);
		}
	}

	public boolean hasAlreadyJoined(int crewId, int memberId) {
		return walkCrewRepository.countByCrewIdAndMemberId(crewId, memberId) > 0;
	}

	public void addMemberToCrew(int crewId, int memberId) {
		walkCrewRepository.insertCrewMember(crewId, memberId);
	}

	public List<WalkCrew> getWalkCrews(int memberId) {
		return walkCrewRepository.getWalkCrews(memberId);
	}

	public List<java.util.Map<String, Object>> getApplicantsByCrewId(int crewId) {
		return walkCrewRepository.getApplicantsByCrewId(crewId);
	}

	public Map<String, Object> getApplicantDetail(int crewId, int memberId) {
		return walkCrewRepository.getApplicantDetail(crewId, memberId);
	}

	public boolean isApprovedMember(int crewId, int memberId) {
		WalkCrew crew = getCrewById(crewId);
		if (crew != null && crew.getLeaderId() == memberId) {
			return true; // ✅ 크루장은 무조건 승인
		}

		return walkCrewMemberRepository.countApprovedMember(crewId, memberId) > 0;
	}

	public void approveMember(int crewId, int memberId) {
		walkCrewRepository.updateMemberStatusToApproved(crewId, memberId);
	}

	public WalkCrew getCrewByLeaderId(int leaderId) {
		return walkCrewRepository.findByLeaderId(leaderId);
	}

	// 크루장만 공지사항쓸수 있다.
	public boolean isCrewLeader(int crewId, int memberId) {
		WalkCrew crew = getCrewById(crewId);
		if (crew == null)
			return false;

		// ✅ 이 로직은 VO가 아니라 서비스 내부에 위치해야 합니다
		return crew.getLeaderId() == memberId;
	}

	public void updateLeader(int crewId, int newLeaderId) {
		walkCrewRepository.updateCrewLeader(crewId, newLeaderId);
	}

	public boolean updateDescription(int crewId, String newDescription) {
		return walkCrewRepository.updateDescriptionById(crewId, newDescription) > 0;
	}

	public List<WalkCrew> getCrewsByLeaderId(int leaderId) {
		return walkCrewRepository.findCrewsByLeaderId(leaderId);
	}

	public List<WalkCrew> getJoinedCrewsByMemberId(int memberId) {
		return walkCrewRepository.findJoinedCrewsByMemberId(memberId);
	}

	// 📍 거리 계산 (lat/lng가 모두 존재하고, 크루 위치도 존재할 경우)
	public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		final int R = 6371;
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

}