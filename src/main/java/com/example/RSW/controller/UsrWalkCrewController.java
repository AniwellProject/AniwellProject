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
import com.example.RSW.vo.WalkCrewMember;
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

	// ✅ 크루 상세보기 페이지 (JSP 반환)
	@GetMapping("/detail/{id}")
	public String showCrewDetail(@PathVariable int id, HttpServletRequest req, Model model) {
		Rq rq = (Rq) req.getAttribute("rq");
		System.out.println("🔥 rq = " + rq);
		System.out.println("🔥 isLogined = " + (rq != null ? rq.isLogined() : "rq가 null임"));

		WalkCrew crew = walkCrewService.getCrewById(id);
		if (crew == null) {
			model.addAttribute("errorMsg", "해당 크루를 찾을 수 없습니다.");
			return "common/error";
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
		boolean isLeader = false;
		boolean isPending = false;

		if (rq != null && rq.isLogined()) {
			int memberId = rq.getLoginedMemberId(); // ✅ 한 번만 선언
			int crewId = crew.getId();

			isJoined = walkCrewMemberService.isJoinedCrew(memberId, crewId);
			isLeader = walkCrewMemberService.isCrewLeader(crewId, memberId);
			isPending = walkCrewMemberService.isPending(crewId, memberId);

			System.out.println("✅ isPending = " + isPending);
		}

		model.addAttribute("crew", crew);
		model.addAttribute("createdDate", createdDate);
		model.addAttribute("crewLocation", crewLocation);
		model.addAttribute("isJoined", isJoined);
		model.addAttribute("isLeader", isLeader);
		model.addAttribute("isPending", isPending);
		model.addAttribute("rq", rq);

		return "usr/walkCrew/detail";
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
	public ResultData getCrewListAsJson(HttpServletRequest req, @RequestParam(required = false) String query, // 🔍 검색어
			@RequestParam(required = false) String dong, // 🏠 동네 이름 (정렬 우선순위용)
			@RequestParam(required = false, defaultValue = "createdAt") String sortBy // 🔃 정렬 기준
	) {
		// ✅ 로그인된 사용자 정보 가져오기
		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ 전체 크루 리스트 조회
		List<WalkCrew> crews = walkCrewService.getAllCrews();

		// ✅ 결과로 반환할 JSON 리스트 준비
		List<Map<String, Object>> resultList = new ArrayList<>();

		// ✅ 크루 리스트를 하나씩 순회
		for (WalkCrew crew : crews) {

			// 🔍 검색어 필터링 (제목 또는 설명에 query가 포함되어 있어야 함)
			if (query != null && !query.isBlank()) {
				boolean titleMatch = crew.getTitle() != null && crew.getTitle().contains(query);
				boolean descMatch = crew.getDescription() != null && crew.getDescription().contains(query);
				if (!titleMatch && !descMatch)
					continue; // 조건에 맞지 않으면 제외
			}

			// ✅ 크루 정보를 Map 형태로 변환
			Map<String, Object> crewMap = new HashMap<>();
			crewMap.put("id", crew.getId());
			crewMap.put("title", crew.getTitle());
			crewMap.put("description", crew.getDescription());
			crewMap.put("nickname", crew.getNickname());
			crewMap.put("city", crew.getCity());
			crewMap.put("district", crew.getDistrict());
			crewMap.put("dong", crew.getDong());
			crewMap.put("createdAt", crew.getCreatedAt());
			crewMap.put("imageUrl", crew.getImageUrl());

			// 🏠 dong 우선 정렬을 위한 비교용 flag 저장
			// ➜ 해당 동네일 경우 true → 나중에 정렬 시 상단으로 배치
			crewMap.put("isTargetDong", dong != null && dong.equals(crew.getDong()));

			// 결과 리스트에 추가
			resultList.add(crewMap);
		}

		// ✅ 정렬 처리 (1순위: 동네 우선, 2순위: sortBy 기준)
		resultList.sort((a, b) -> {
			boolean aIsTarget = (boolean) a.getOrDefault("isTargetDong", false);
			boolean bIsTarget = (boolean) b.getOrDefault("isTargetDong", false);

			// 💡 1순위: dong이 일치하는 항목을 최상단에 배치
			if (aIsTarget && !bIsTarget)
				return -1;
			if (!aIsTarget && bIsTarget)
				return 1;

			// 💡 2순위: 정렬 기준에 따라 정렬 (기본은 createdAt 내림차순)
			if (sortBy.equals("title")) {
				return ((String) a.get("title")).compareTo((String) b.get("title")); // 가나다순
			} else {
				return ((Comparable) b.get("createdAt")).compareTo(a.get("createdAt")); // 최신순
			}
		});

		// ✅ 반환 데이터 구성
		Map<String, Object> data = new HashMap<>();
		data.put("crews", resultList); // 정렬된 크루 리스트
		data.put("loginMemberId", (rq != null && rq.isLogined()) ? rq.getLoginedMemberId() : ""); // 로그인된 사용자 ID

		// ✅ JSON 응답 반환
		return ResultData.from("S-1", "크루 목록 불러오기 성공", data);
	}

	// ✅ 크루 소개글 수정 처리
	@PostMapping("/doModifyDescription")
	@ResponseBody
	public ResultData modifyCrewDescription(@RequestParam int crewId, @RequestParam String newDescription,
			HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ 로그인 체크
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인 후 이용해주세요.");
		}

		int memberId = rq.getLoginedMemberId();

		// ✅ 크루장만 수정 가능
		boolean isLeader = walkCrewMemberService.isCrewLeader(crewId, memberId);
		if (!isLeader) {
			return ResultData.from("F-2", "크루장만 소개글을 수정할 수 있습니다.");
		}

		// ✅ 실제 수정 로직 수행
		boolean result = walkCrewService.updateDescription(crewId, newDescription);
		if (!result) {
			return ResultData.from("F-3", "소개글 수정에 실패했습니다.");
		}

		Map<String, Object> data = new HashMap<>();
		data.put("crewId", crewId);
		data.put("newDescription", newDescription);

		return ResultData.from("S-1", "소개글이 성공적으로 수정되었습니다.", data);
	}

}