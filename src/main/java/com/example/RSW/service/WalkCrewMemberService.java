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

@Service
public class WalkCrewMemberService {

	@Autowired
	WalkCrewMemberRepository walkCrewMemberRepository;

	// ✅ 크루 참가 요청
	public void requestToJoinCrew(int crewId, int memberId) {
		walkCrewMemberRepository.requestToJoinCrew(crewId, memberId);
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

}