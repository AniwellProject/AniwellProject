package com.example.RSW.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.RSW.service.ArticleService;
import com.example.RSW.service.NotificationService;
import com.example.RSW.service.WalkCrewMemberService;
import com.example.RSW.service.WalkCrewService;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.WalkCrewMember;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/usr/walkCrewMember")
public class UsrWalkCrewMemberController {

	@Autowired
	ArticleService articleService;

	@Autowired
	NotificationService notificationService;

	private final WalkCrewService walkCrewService;
	private final WalkCrewMemberService walkCrewMemberService;

	@Autowired
	public UsrWalkCrewMemberController(WalkCrewService walkCrewService, WalkCrewMemberService walkCrewMemberService) {
		this.walkCrewService = walkCrewService;
		this.walkCrewMemberService = walkCrewMemberService;
	}

	// ✅ 크루 메뉴용 공통 데이터
	@ResponseBody
	@GetMapping("/usr/walkCrew/myCrew")
	public ResultData getCrewForMenu(HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용 가능합니다.");
		}

		int memberId = rq.getLoginedMemberId();
		System.out.println(memberId);

		// getTitle()은 롬복 @Data로 자동 생성됨
		WalkCrew crew = walkCrewService.getCrewByLeaderId(memberId);
		if (crew != null) {
			// ✅ Java 8 호환: Map.of → HashMap
			Map<String, Object> data = new HashMap<>();
			data.put("crewId", crew.getId());
			data.put("title", crew.getTitle());
			data.put("city", crew.getCity());
			data.put("district", crew.getDistrict());
			data.put("dong", crew.getDong());

			return ResultData.from("S-1", "크루장 정보입니다.", data);
		}

		crew = walkCrewMemberService.getMyCrew(memberId);
		if (crew != null) {
			// ✅ Java 8 호환: Map.of → HashMap
			Map<String, Object> data = new HashMap<>();
			data.put("crewId", crew.getId());
			data.put("title", crew.getTitle());
			data.put("city", crew.getCity());
			data.put("district", crew.getDistrict());
			data.put("dong", crew.getDong());

			return ResultData.from("S-2", "크루 멤버 정보입니다.", data);
		}

		return ResultData.from("F-2", "가입된 크루가 없습니다.");
	}

	// ✅ 참가 요청
	@PostMapping("/doJoin")
	@ResponseBody
	public ResultData doJoin(@RequestParam int crewId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인이 필요합니다.");
		}

		int memberId = rq.getLoginedMemberId();

		// ✅ 중복 신청 방지 로직을 포함한 서비스 호출
		ResultData resultData = walkCrewMemberService.requestToJoinCrew(crewId, memberId);

		// ✅ 실패 시 그대로 반환
		if (resultData.isFail()) {
			return resultData;
		}

		// ✅ 크루장에게 알림 전송
		notificationService.notifyCrewLeaderOnRequest(crewId, memberId);

		// ✅ 성공 시 응답 데이터 구성
		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("memberId", memberId);

		return ResultData.from("S-1", "크루 참가 요청이 완료되었습니다.", data);
	}

	// ✅ 신청자 리스트 (크루장만)
	@GetMapping("/requestList")
	@ResponseBody
	public ResultData showRequestList(@RequestParam int crewId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		int loginedMemberId = rq.getLoginedMemberId();
		WalkCrew crew = walkCrewService.getCrewById(crewId);

		if (crew == null) {
			return ResultData.from("F-2", "해당 크루가 존재하지 않습니다.");
		}

		if (crew.getLeaderId() != loginedMemberId) {
			return ResultData.from("F-3", "해당 페이지에 접근 권한이 없습니다.");
		}

		List<Map<String, Object>> applicants = walkCrewService.getApplicantsByCrewId(crewId);

		// ✅ Java 8 호환: Map.of(...) → HashMap 사용
		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("applicants", applicants);

		return ResultData.from("S-1", "신청자 목록 조회 성공", data);
	}

	// ✅ 신청자 상세 정보 보기
	@GetMapping("/requestDetail")
	@ResponseBody
	public ResultData showRequestDetail(@RequestParam int crewId, @RequestParam int memberId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		System.out.println(memberId);
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew == null) {
			return ResultData.from("F-2", "크루 정보를 찾을 수 없습니다.");
		}

		if (crew.getLeaderId() != rq.getLoginedMemberId()) {
			return ResultData.from("F-3", "해당 페이지에 접근 권한이 없습니다.");
		}

		Map<String, Object> applicant = walkCrewService.getApplicantDetail(crewId, memberId);
		if (applicant == null) {
			return ResultData.from("F-4", "신청자 정보를 찾을 수 없습니다.");
		}

		// ✅ Java 8 호환: Map.of(...) 대신 HashMap 사용
		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("memberId", memberId);
		data.put("applicant", applicant);

		return ResultData.from("S-1", "신청자 상세 정보 조회 성공", data);
	}

	// ✅ 참가 신청 수락 처리
	@PostMapping("/approve")
	@ResponseBody
	public ResultData approveApplicant(@RequestParam int crewId, @RequestParam int memberId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew == null) {
			return ResultData.from("F-2", "해당 크루를 찾을 수 없습니다.");
		}

		if (crew.getLeaderId() != rq.getLoginedMemberId()) {
			return ResultData.from("F-3", "해당 크루의 리더만 수락할 수 있습니다.");
		}

		// ✅ 수락 처리
		walkCrewService.approveMember(crewId, memberId);

		// ✅ 수락된 신청자에게 알림 전송
		notificationService.notifyMemberOnCrewAccepted(crewId, memberId);

		// ✅ Java 8 호환: Map.of(...) → HashMap
		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("memberId", memberId);
		data.put("redirectUrl", "/usr/walkCrewMember/myCrewCafe");

		// ✅ 프론트가 이 URL로 이동할 수 있도록 안내
		return ResultData.from("S-1", "참가 신청을 수락했습니다.", data);
	}

	// ✅ 크루 멤버 강퇴 처리 (크루장만 가능)
	@PostMapping("/expel")
	@ResponseBody
	public ResultData expelCrewMember(@RequestParam int crewId, @RequestParam int memberId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ 로그인 여부 확인
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		// ✅ 크루 존재 및 권한 확인
		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew == null) {
			return ResultData.from("F-2", "해당 크루가 존재하지 않습니다.");
		}

		if (crew.getLeaderId() != rq.getLoginedMemberId()) {
			return ResultData.from("F-3", "크루장만 멤버를 강퇴할 수 있습니다.");
		}

		// ✅ 강퇴 처리 (아직 서비스/리포지토리 구현 필요)
		boolean result = walkCrewMemberService.expelMemberFromCrew(crewId, memberId);
		if (!result) {
			return ResultData.from("F-4", "강퇴에 실패했습니다. 이미 탈퇴했거나 존재하지 않는 멤버일 수 있습니다.");
		}

		// ✅ Java 8 호환 응답
		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("memberId", memberId);

		return ResultData.from("S-1", "크루 멤버가 성공적으로 강퇴되었습니다.", data);
	}

	@PostMapping("/leave")
	@ResponseBody
	public ResultData leaveCrew(@RequestParam int crewId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인이 필요합니다.");
		}

		int memberId = rq.getLoginedMemberId();

		// ✅ 현재 역할 확인
		String role = walkCrewMemberService.getRole(memberId, crewId);
		if (role == null) {
			return ResultData.from("F-2", "해당 크루에 가입되어 있지 않습니다.");
		}

		// ✅ 크루장은 탈퇴 불가 (위임 후 가능)
		if (role.equals("leader")) {
			return ResultData.from("F-3", "크루장은 위임 후에만 탈퇴할 수 있습니다.");
		}

		// ✅ 탈퇴 처리
		boolean result = walkCrewMemberService.expelMemberFromCrew(crewId, memberId);
		if (!result) {
			return ResultData.from("F-4", "탈퇴 처리 중 오류가 발생했습니다.");
		}

		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("memberId", memberId);

		return ResultData.from("S-1", "크루 탈퇴가 완료되었습니다.", data);
	}

	@GetMapping("/usr/walkCrew/memberList")
	@ResponseBody
	public ResultData getMemberList(@RequestParam int crewId) {
		List<WalkCrewMember> members = walkCrewMemberService.getMembersByCrewId(crewId);
		return ResultData.from("S-1", "크루 멤버 리스트", members);
	}

	// ✅ 크루장 권한 위임 기능
	@PostMapping("/transferLeadership")
	@ResponseBody
	public ResultData transferLeadership(@RequestParam int crewId, @RequestParam int newLeaderId,
			HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		System.out.println("newLeaderId" + newLeaderId);
		// 로그인 확인
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		int currentLeaderId = rq.getLoginedMemberId();

		// 본인에게 위임 불가
		if (currentLeaderId == newLeaderId) {
			return ResultData.from("F-2", "본인에게는 위임할 수 없습니다.");
		}

		// 현재 리더인지 확인
		String currentRole = walkCrewMemberService.getRole(currentLeaderId, crewId);
		if (!"leader".equals(currentRole)) {
			return ResultData.from("F-3", "크루장만 권한을 위임할 수 있습니다.");
		}

		// 대상 멤버가 크루 멤버인지 확인
		if (!walkCrewMemberService.isMemberOfCrew(newLeaderId, crewId)) {
			return ResultData.from("F-4", "해당 멤버는 이 크루의 멤버가 아닙니다.");
		}

		// 위임 처리
		boolean success = walkCrewMemberService.transferLeadership(crewId, currentLeaderId, newLeaderId);
		if (!success) {
			return ResultData.from("F-5", "위임 처리에 실패했습니다.");
		}

		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("newLeaderId", newLeaderId);

		return ResultData.from("S-1", "크루장 권한이 성공적으로 위임되었습니다.", data);
	}

	// 크루가입 신청취소
	@PostMapping("/cancelJoin")
	@ResponseBody
	public ResultData cancelJoin(@RequestParam int crewId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		int memberId = rq.getLoginedMemberId();
		boolean result = walkCrewMemberService.cancelJoin(crewId, memberId);
		if (result) {
			return ResultData.from("S-1", "신청이 취소되었습니다.");
		} else {
			return ResultData.from("F-2", "신청 취소에 실패했습니다.");
		}
	}

}
