package com.example.RSW.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.util.Ut;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.config.AppConfig;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.service.DistrictService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.WalkCrewMemberService;
import com.example.RSW.service.WalkCrewService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Controller
@RequestMapping("/usr/walkCrew")
public class UsrWalkCrewController {

	@Autowired
	private DistrictService districtService;

	@Autowired
	private DistrictRepository districtRepository;

	@Autowired
	private WalkCrewMemberService walkCrewMemberService;

	@Autowired
	private Cloudinary cloudinary;

	private final WalkCrewService walkCrewService;

	// ✅ AppConfig에서 Kakao Key 가져오기 위한 DI
	@Autowired
	private AppConfig appConfig; // @Value 주입된 클래스

	// 크루 목록 페이지 이동
	@GetMapping("/list")
	public String showCrewList(HttpServletRequest req, Model model) {
		Rq rq = (Rq) req.getAttribute("rq");

		List<WalkCrew> crews = walkCrewService.getAllCrews(); // 전체 크루 목록 조회

		model.addAttribute("crews", crews);
		model.addAttribute("loginMemberId", (rq != null && rq.isLogined()) ? rq.getLoginedMemberId() : "");

		return "usr/walkCrew/list"; // JSP 뷰 경로
	}

	// ✅ 크루 등록 폼 페이지 출력
	@GetMapping("/create")
	public String showCreateForm(HttpServletRequest req, Model model) {
		model.addAttribute("kakaoJsKey", appConfig.getKakaoJavascriptKey());
		return "usr/walkCrew/create"; // JSP 경로
	}

	@PostMapping("/doCreate")
	@ResponseBody
	public ResultData doCreateCrew(@RequestParam("title") String title, @RequestParam("description") String description,
			@RequestParam("districtId") int districtId, @RequestParam("selectedDong") String dong,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		WalkCrew walkCrew = new WalkCrew();
		walkCrew.setTitle(title);
		walkCrew.setDescription(description);
		walkCrew.setDistrictId(districtId);
		walkCrew.setDong(dong);
		walkCrew.setLeaderId(rq.getLoginedMemberId());

		// ✅ 이미지 업로드 - Cloudinary
		if (imageFile != null && !imageFile.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
				String imageUrl = (String) uploadResult.get("secure_url");
				walkCrew.setImageUrl(imageUrl); // VO에 필드가 있어야 함
			} catch (Exception e) {
				return ResultData.from("F-2", "이미지 업로드 중 오류 발생");
			}
		}

		// ✅ 크루 등록
		walkCrewService.createCrew(walkCrew);

		Map<String, Object> data = new HashMap<>();
		data.put("crewId", walkCrew.getId());

		return ResultData.from("S-1", "크루 생성 완료", data);
	}

	// 크루 상세보기 페이지
	// ✅ 크루 상세보기 페이지 (JSP 반환)
	@GetMapping("/detail/{id}")
	public String showCrewDetail(@PathVariable int id, HttpServletRequest req, Model model) {
		Rq rq = (Rq) req.getAttribute("rq");

		WalkCrew crew = walkCrewService.getCrewById(id);
		if (crew == null) {
			model.addAttribute("errorMsg", "해당 크루를 찾을 수 없습니다.");
			return "common/error"; // 에러 페이지
		}

		Date createdDate = Date.from(crew.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());

		String crewLocation = "";
		if (crew.getDistrictId() != 0) {
			District district = districtService.findById(crew.getDistrictId());
			if (district != null) {
				crewLocation = district.getSido() + " " + district.getSigungu() + " " + district.getDong();
			}
		}

		boolean isJoined = false;
		if (rq != null && rq.isLogined()) {
			isJoined = walkCrewMemberService.isJoinedCrew(rq.getLoginedMemberId(), crew.getId());
		}

		model.addAttribute("crew", crew);
		model.addAttribute("createdDate", createdDate);
		model.addAttribute("crewLocation", crewLocation);
		model.addAttribute("isJoined", isJoined);
		model.addAttribute("rq", rq);

		return "usr/walkCrew/detail"; // JSP 경로
	}

	// ✅ 크루 참가 처리
	@PostMapping("/join")
	@ResponseBody
	public ResultData joinCrew(@RequestParam("crewId") int crewId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ 로그인 여부 체크
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		int memberId = rq.getLoginedMemberId();

		// ✅ 이미 참가했는지 여부 체크
		if (walkCrewService.hasAlreadyJoined(crewId, memberId)) {
			return ResultData.from("F-2", "이미 참가한 크루입니다.");
		}

		// ✅ 참가 처리
		walkCrewService.addMemberToCrew(crewId, memberId);

		// ✅ Java 8 호환: Map.of(...) 대신 HashMap 사용
		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);

		// ✅ 성공 응답 반환
		return ResultData.from("S-1", "참가 신청이 완료되었습니다.", data);
	}

	// ✅ 특정 시, 구에 해당하는 동 목록 반환 (Ajax)
	@GetMapping("/getDongs")
	@ResponseBody
	public ResultData getDongs(@RequestParam String city, @RequestParam String district) {
		List<String> dongs = districtService.findDongsByCityAndDistrict(city, district);

		Map<String, Object> data = new HashMap<>();
		data.put("dongs", dongs);

		return ResultData.from("S-1", "동 목록 조회 성공", data);
	}

	// ✅ 선택된 시/구/동에 해당하는 districtId 반환 (Ajax)
	@GetMapping("/getDistrictId")
	@ResponseBody
	public ResultData getDistrictId(@RequestParam String city, @RequestParam String district,
			@RequestParam String dong) {
		int id = districtRepository.getDistrictIdByFullAddress(city, district, dong);

		Map<String, Object> data = new HashMap<>();
		data.put("districtId", id);

		return ResultData.from("S-1", "지역 ID 조회 성공", data);
	}

	// 참가 요청 권한
	@PostMapping("/approveApplicant")
	@ResponseBody
	public ResultData approveApplicant(@RequestParam int crewId, @RequestParam int memberId) {
		walkCrewService.approveMember(crewId, memberId);

		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("memberId", memberId);

		return ResultData.from("S-1", "참가 요청을 수락했습니다.", data);
	}

	// ✅ 크루 목록을 JSON 형태로 반환하는 API 컨트롤러
	@GetMapping("/api/list")
	@ResponseBody
	public ResultData getCrewListAsJson(HttpServletRequest req) {
		// 🔹 로그인 사용자 정보 가져오기 (Rq는 로그인 상태 확인용 커스텀 객체)
		Rq rq = (Rq) req.getAttribute("rq");

		// 🔹 모든 크루 정보를 데이터베이스에서 조회
		List<WalkCrew> crews = walkCrewService.getAllCrews();

		// 🔹 프론트에 반환할 JSON 형태로 변환할 리스트 선언
		List<Map<String, Object>> resultList = new ArrayList<>();

		// 🔁 각 크루 정보를 Map 형태로 변환해서 리스트에 담기
		for (WalkCrew crew : crews) {
			Map<String, Object> crewMap = new HashMap<>();

			// ▶️ 크루 기본 정보 저장
			crewMap.put("id", crew.getId());
			crewMap.put("title", crew.getTitle());
			crewMap.put("description", crew.getDescription());
			crewMap.put("nickname", crew.getNickname());
			crewMap.put("city", crew.getCity());
			crewMap.put("district", crew.getDistrict());
			crewMap.put("dong", crew.getDong());
			crewMap.put("createdAt", crew.getCreatedAt());

			// ✅ 핵심: 이미지 URL도 포함해야 프론트에서 썸네일 출력 가능
			crewMap.put("imageUrl", crew.getImageUrl());

			// ▶️ 완성된 crewMap을 결과 리스트에 추가
			resultList.add(crewMap);
		}

		// 🔹 최종 반환용 data 객체 생성 (crews 리스트 + 로그인한 사용자 ID 포함)
		Map<String, Object> data = new HashMap<>();
		data.put("crews", resultList); // 크루 목록 데이터
		data.put("loginMemberId", (rq != null && rq.isLogined()) ? rq.getLoginedMemberId() : "");

		// 🔚 ResultData 포맷으로 응답 반환
		return ResultData.from("S-1", "크루 목록 불러오기 성공", data);
	}

}