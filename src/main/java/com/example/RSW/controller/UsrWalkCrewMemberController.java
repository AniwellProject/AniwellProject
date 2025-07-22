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
import com.example.RSW.service.WalkCrewMemberService;
import com.example.RSW.service.WalkCrewService;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/usr/walkCrewMember")
public class UsrWalkCrewMemberController {

	@Autowired
	ArticleService articleService;

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
		walkCrewMemberService.requestToJoinCrew(crewId, memberId);

		// ✅ Java 8 호환: Map.of(...) → HashMap 사용
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

	// ✅ 크루 탈퇴 (멤버 본인 요청)
	@PostMapping("/leave")
	@ResponseBody
	public ResultData leaveCrew(@RequestParam int crewId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ 로그인 확인
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인이 필요합니다.");
		}

		int memberId = rq.getLoginedMemberId();

		// ✅ 이미 크루에 없는 경우 예외 처리
		boolean isMember = walkCrewMemberService.isJoinedCrew(memberId, crewId);
		if (!isMember) {
			return ResultData.from("F-2", "해당 크루에 가입되어 있지 않습니다.");
		}

		// ✅ 탈퇴 처리 (삭제)
		boolean result = walkCrewMemberService.expelMemberFromCrew(crewId, memberId);
		if (!result) {
			return ResultData.from("F-3", "탈퇴 처리 중 오류가 발생했습니다.");
		}

		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("memberId", memberId);

		return ResultData.from("S-1", "크루 탈퇴가 완료되었습니다.", data);
	}

}
