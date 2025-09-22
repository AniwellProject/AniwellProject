package com.example.RSW.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.service.*;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PetController {

	private static final Set<String> ALLOWED_TYPES = Set.of("병원", "용품", "애견용품", "동물병원", "애견카페", "공원", "펫호텔", "미정");

	@Autowired
	Rq rq;

	@Autowired
	private PetRecommendationService petRecommendationService;

	@Autowired
	private PetService petService;

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private PetVaccinationService petVaccinationService;

	@Autowired
	private PetAnalysisService petAnalysisService;

	@Autowired
	private CalendarEventService calendarEventService;

	@Autowired
	private Cloudinary cloudinary;

	@Autowired
	private DocComposeService composeService;

	@Autowired
	private MedicalDocumentService medicalDocumentService;
	@Autowired
	private VisitService visitService;

	// 추천 장소 리스트 불러오기
	@GetMapping("/usr/pet/recommend/list")
	@ResponseBody
	public List<PetRecommendation> getAllFavorites(@RequestParam int memberId) {
		return petRecommendationService.getFavoriteNamesByMember(memberId);
	}

	// 즐겨 찾기 삭제 로직
	@RequestMapping("/usr/pet/recommend/toggle")
	@ResponseBody
	public String toggleFavorite(@RequestParam int memberId, @RequestParam String name,
			@RequestParam(required = false) String type, @RequestParam(required = false) String address,
			@RequestParam(required = false) String phone, @RequestParam(required = false) String mapUrl) {
		if (type == null || !ALLOWED_TYPES.contains(type)) {
			type = "미정";
		}

		boolean exists = petRecommendationService.isFavorited(memberId, name);
		if (exists) {
			petRecommendationService.removeFavorite(memberId, name);
			return "removed";
		} else {
			petRecommendationService.saveFavorite(memberId, type, name, address, phone, mapUrl);
			return "added";
		}
	}

	// 장소 디테일 불러오기
	@RequestMapping("/usr/pet/test")
	@ResponseBody
	public Map<String, Object> getPlaceDetail(@RequestParam String url) {
		return KakaoPlaceCrawler.crawlPlace(url);
	}

	// 주변 펫 샵 조회
	@RequestMapping("/usr/pet/petPlace")
	public String showMap(Model model, HttpServletResponse resp) throws IOException {
		int memberId = rq.getLoginedMemberId();

		List<String> favoriteNames = petRecommendationService.getFavoriteNamesOnly(memberId);
		model.addAttribute("favoriteNames", favoriteNames); // 분류 타입만 불러오기

		List<PetRecommendation> favoriteplaces = petRecommendationService.getFavoriteNamesByMember(memberId);
		model.addAttribute("favoriteplaces", favoriteplaces); // 추천장소 객체
		model.addAttribute("memberId", memberId); // 로그인 아이디
		return "usr/pet/petPlace";
	}

	// 펫 상세페이지
	@RequestMapping("/usr/pet/petPage")
	public String showTest(@RequestParam("petId") int petId, Model model, HttpServletResponse resp) throws IOException {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			resp.setContentType("text/html; charset=UTF-8");
			resp.getWriter().write(Ut.jsHistoryBack("F-1", "권한이 없습니다."));
			return null;
		}
		model.addAttribute("pet", pet);

		List<PetVaccination> list = petVaccinationService.getVaccinationsByPetId(petId);

		List<Map<String, Object>> events = new ArrayList<>();
		for (PetVaccination pv : list) {
			// 접종 이벤트
			Map<String, Object> injEvent = new HashMap<>();
			injEvent.put("id", pv.getId());
			injEvent.put("title", pv.getVaccineName() + " 접종");
			injEvent.put("start", pv.getInjectionDate().toString());
			injEvent.put("color", "#4caf50"); // 캘린더 표시 색 (변경 가능)

			events.add(injEvent); // 접종한 백신 데이터

			// 다음 예정 이벤트 (이름이 같은 백신 들어오면 마지막 접종의 다음 날짜만 표시)
			if (pv.getNextDueDate() != null) {
				Map<String, Object> nextEvent = new HashMap<>();
				nextEvent.put("id", pv.getId());
				nextEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 다음 예정");
				nextEvent.put("start", pv.getNextDueDate().toString());
				nextEvent.put("color", "#f44336"); // 캘린더 표시 색 (변경 가능)

				events.add(nextEvent); // 최종적인 다음 날짜
			}
		}

		ObjectMapper objectMapper = new ObjectMapper();
		model.addAttribute("eventsJson", events); // 접종이벤트 내용 넘김
		return "usr/pet/petPage"; // JSP or Thymeleaf 페이지
	}

	// 등록한 펫 목록 / 가입한 크루 목록
	@RequestMapping("/usr/pet/list")
	public String showPetList(@RequestParam("memberId") int memberId, Model model, HttpServletResponse resp)
			throws IOException {
		int loginId = rq.getLoginedMemberId();
		if (loginId != memberId) {
			resp.setContentType("text/html; charset=UTF-8");
			resp.getWriter().write(Ut.jsHistoryBack("F-1", "권한이 없습니다."));
			return null; // 페이지 이동 막기
		}
		List<Pet> pets = petService.getPetsByMemberId(memberId);
		List<WalkCrew> crews = walkCrewService.getWalkCrews(memberId);

		Member loginesMember = rq.getLoginedMember();

		model.addAttribute("member", loginesMember); // 로그인 멤버
		model.addAttribute("pets", pets); // 해당 멤버가 등록한 펫ID
		model.addAttribute("crews", crews); // 해당 멤버가 가입한 크루목록
		return "usr/pet/list"; // JSP or Thymeleaf 페이지
	}

	// petlist JSON방식으로 받아오
	@RequestMapping("/api/pets")
	@ResponseBody
	public ResultData getPetList(@RequestParam("memberId") int memberId) {
		int loginId = rq.getLoginedMemberId();
		if (loginId != memberId) {
			return ResultData.from("F-1", "권한이 없습니다.");
		}
		List<Pet> pets = petService.getPetsByMemberId(memberId);
		List<WalkCrew> crews = walkCrewService.getWalkCrews(memberId);

		Member loginesMember = rq.getLoginedMember();

		return ResultData.from("S-1", "로그인멤버/펫/크루목록", "member", loginesMember, "pets", pets, "crews", crews); // JSP or
																												// Thymeleaf
																												// 페이지
	}

	// 펫등록 페이지 이동
	@RequestMapping("/usr/pet/join")
	public String showJoin() {
		return "/usr/pet/join";
	}

	// 펫 등록 로직

	@RequestMapping("/usr/pet/doJoin")
	@ResponseBody
	public String doJoin(HttpServletRequest req, @RequestParam("photo") MultipartFile photo, @RequestParam String name,
			@RequestParam String species, @RequestParam String breed, @RequestParam String gender,
			@RequestParam String birthDate, @RequestParam double weight,
			@RequestParam(value = "feedType", required = false) String feedType,
			@RequestParam(value = "brand", required = false) String brand,
			@RequestParam(value = "productName", required = false) String productName,
			@RequestParam(value = "flavor", required = false) String flavor) {
		if (Ut.isEmptyOrNull(name))
			return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
		if (Ut.isEmptyOrNull(species))
			return Ut.jsHistoryBack("F-2", "종을 입력하세요");
		if (Ut.isEmptyOrNull(breed))
			return Ut.jsHistoryBack("F-3", "품종을 입력하세요");
		if (Ut.isEmptyOrNull(gender))
			return Ut.jsHistoryBack("F-4", "성별을 입력하세요");
		if (Ut.isEmptyOrNull(birthDate))
			return Ut.jsHistoryBack("F-5", "생일을 입력하세요");
		if (Ut.isEmptyOrNull(String.valueOf(weight)))
			return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

		String imagePath = null;
		if (!photo.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
				imagePath = (String) uploadResult.get("secure_url");
			} catch (IOException e) {
				e.printStackTrace();
				return Ut.jsHistoryBack("F-7", "사진 업로드 중 오류 발생");
			}
		}

		ResultData joinRd = petService.insertPet(rq.getLoginedMemberId(), name, species, breed, gender, birthDate,
				weight, imagePath);

		// 방금 만든 펫 PK
		Integer petId = petService.findNewestPetIdByMemberAndName(rq.getLoginedMemberId(), name);

		// 기본사료 자동 전환 (brand & feedType 둘 다 있을 때만)
		if (petId != null && brand != null && !brand.isBlank() && feedType != null && !feedType.isBlank()) {
			try {
				// feedType 정규화
				String normalized = feedType.trim();
				if ("건식".equals(normalized))
					normalized = "dry";
				else if ("습식".equals(normalized))
					normalized = "wet";

				// DB 제약 대응용 안전값
				String safeBrand = brand.trim();
				String safeProductName = (productName != null && !productName.isBlank()) ? productName.trim()
						: safeBrand; // productName NOT NULL이면 브랜드로 대체
				String safeFlavor = (flavor != null) ? flavor.trim() : ""; // NOT NULL이면 "" 권장

				// 기본사료 종료/시작 (트랜잭션 내부)
				petService.upsertPrimaryFoodIfChanged(petId, safeBrand, normalized, safeProductName, safeFlavor);

				// (옵션) 급여 이벤트도 기록하고 싶으면 사용
				// petService.insertFeedEvent(petId, normalized, safeBrand);

			} catch (Exception e) {
				System.err.println("[WARN] doJoin 사료 처리 실패" + " | petId=" + petId + " | brand=" + brand + " | feedType="
						+ feedType + " | productName=" + productName + " | flavor=" + flavor);
				e.printStackTrace();
				// 등록 자체는 성공이므로 사료 오류는 로그만
			}
		}

		return Ut.rd("S-1", "등록되었습니다!");
	}

	// 펫 정보 수정 페이지로 이동

	@RequestMapping("/usr/pet/modify")
	public String showModify(@RequestParam("petId") int petId, Model model, HttpServletResponse resp)
			throws IOException {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			resp.setContentType("text/html; charset=UTF-8");
			resp.getWriter().write(Ut.jsHistoryBack("F-1", "권한이 없습니다."));
			return null;
		}

		model.addAttribute("pet", pet);
		return "usr/pet/modify";
	}

	// 펫 정보 수정 로직
	@RequestMapping("/usr/pet/doModify")
	@ResponseBody
	public String doModify(HttpServletRequest req, @RequestParam("petId") int petId, String name, String species,
			String breed, String gender, String birthDate, double weight, MultipartFile photo,
			@RequestParam(value = "feedType", required = false) String feedType, // 'dry' | 'wet' (또는 한글)
			@RequestParam(value = "brand", required = false) String brand, // 브랜드명
			@RequestParam(value = "productName", required = false) String productName, // ★ 추가
			@RequestParam(value = "flavor", required = false) String flavor // ★ 추가
	) {

		System.out.println(feedType);
		System.out.println(brand);
		System.out.println(productName);
		System.out.println(flavor);

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-0", "권한이 없습니다.");
		}

		if (Ut.isEmptyOrNull(name))
			return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
		if (Ut.isEmptyOrNull(species))
			return Ut.jsHistoryBack("F-2", "종을 입력하세요");
		if (Ut.isEmptyOrNull(breed))
			return Ut.jsHistoryBack("F-3", "품종을 입력하세요");
		if (Ut.isEmptyOrNull(gender))
			return Ut.jsHistoryBack("F-4", "성별을 입력하세요");
		if (Ut.isEmptyOrNull(birthDate))
			return Ut.jsHistoryBack("F-5", "생일을 입력하세요");
		if (Ut.isEmptyOrNull(String.valueOf(weight)))
			return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

		String photoPath = null;
		if (photo != null && !photo.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
				photoPath = (String) uploadResult.get("secure_url");
			} catch (IOException e) {
				e.printStackTrace();
				return Ut.jsHistoryBack("F-1", "사진 업로드 실패");
			}
		}

		ResultData modifyRd;
		if (photoPath == null) {
			modifyRd = petService.updatePetyWithoutPhoto(petId, name, species, breed, gender, birthDate, weight);
		} else {
			modifyRd = petService.updatePet(petId, name, species, breed, gender, birthDate, weight, photoPath);
		}

		// 체중 로그 (실패해도 메인흐름 유지)
		try {
			petService.insertWeightAlways(petId, weight, "manual", "수정화면 업데이트");
		} catch (Exception e) {
			System.err.println("[ERROR] 몸무게 기록 저장 실패 | petId=" + petId);
			e.printStackTrace();
		}

		// feedType 정규화
		if (feedType != null) {
			feedType = feedType.trim();
			if ("건식".equals(feedType))
				feedType = "dry";
			else if ("습식".equals(feedType))
				feedType = "wet";
		}

		// ★ productName/flavor 안전 기본값 (DB가 NOT NULL이면 최소 빈문자라도)
		String safeBrand = (brand != null) ? brand.trim() : null;
		String safeProductName = (productName != null && !productName.isBlank()) ? productName.trim()
				: (safeBrand != null ? safeBrand : ""); // 브랜드로 대체 또는 ""
		String safeFlavor = (flavor != null) ? flavor.trim() : ""; // NOT NULL이면 "" 권장

		// 기본사료 전환 (브랜드/타입 둘 다 있을 때만)
		try {
			if (safeBrand != null && !safeBrand.isEmpty() && feedType != null && !feedType.isEmpty()) {

				// ★ productName, flavor까지 서비스로 전달
				petService.upsertPrimaryFoodIfChanged(petId, safeBrand, feedType, safeProductName, safeFlavor);

				// 급여 이벤트를 남기고 싶으면 주석 해제
				// petService.insertFeedEvent(petId, feedType, safeBrand);
			}
		} catch (Exception e) {
			System.err.println("[ERROR] 사료 이벤트/기본사료 처리 실패" + " | petId=" + petId + " | brand=" + safeBrand
					+ " | feedType=" + feedType + " | productName=" + safeProductName + " | flavor=" + safeFlavor);
			e.printStackTrace();
		}

		return Ut.rd("S-1", "수정되었습니다!");
	}

	// 감정 갤러리 이동
	@RequestMapping("/usr/pet/gallery")
	public String showGallery(@RequestParam("petId") int petId, Model model, HttpServletResponse resp)
			throws IOException {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			resp.setContentType("text/html; charset=UTF-8");
			resp.getWriter().write(Ut.jsHistoryBack("F-1", "권한이 없습니다."));
			return null;
		}
		List<PetAnalysis> analysisList = petAnalysisService.getAnalysisByPetId(petId);

		List<String> emotionList = List.of("happy", "relaxed", "sad", "scared");
		model.addAttribute("emotionList", emotionList);
		model.addAttribute("analysisList", analysisList);
		return "usr/pet/gallery";
// 분석 요청 form (이미지 경로 선택)
	}

	// 감정 분석 로직
	@PostMapping("/usr/pet/analysis/do")
	@ResponseBody
	public Map<String, Object> doAnalysis(@RequestParam("petId") int petId, @RequestParam("species") String species,
			@RequestParam("imageFile") MultipartFile imageFile) {

		Map<String, Object> result = new HashMap<>();
		try {
			// 1. Cloudinary 업로드
			Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
			String imageUrl = (String) uploadResult.get("secure_url");

			// 2. 임시 파일로 저장해서 파이썬에 전달
			File tempFile = File.createTempFile("emotion_", ".jpg");
			imageFile.transferTo(tempFile);

			// 3. 종에 따라 파이썬 파일 선택
			String scriptPath;
			if ("강아지".equals(species)) {
				scriptPath = "/Users/e-suul/Desktop/ESeul-main/dog_pickture.py";
			} else {
				scriptPath = "/Users/e-suul/Desktop/ESeul-main/cat_test.py";
			}

			// 4. 파이썬 실행
			String command = "python3 " + scriptPath + " " + tempFile.getAbsolutePath();
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
			String line;
			String lastLine = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("🐍 Python output: " + line);
				lastLine = line;
			}

			process.waitFor();
			System.out.println("✅ 파이썬 종료 코드: " + process.exitValue());
			System.out.println("⚠ 최종 파이썬 결과 문자열: " + lastLine);

			if (lastLine == null || !lastLine.trim().startsWith("{")) {
				throw new RuntimeException("❌ 파이썬 실행 실패 또는 JSON 형식 아님");
			}

			// 5. JSON 파싱
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(lastLine);
			String emotion = root.get("emotion").asText();
			double confidence = root.get("probabilities").get(emotion).asDouble();

			// 6. DB 저장
			PetAnalysis analysis = new PetAnalysis();
			analysis.setPetId(petId);
			analysis.setImagePath(imageUrl); // Cloudinary URL 저장
			analysis.setEmotionResult(emotion);
			analysis.setConfidence(confidence);
			petAnalysisService.save(analysis);

			// 7. 응답 반환
			result.put("emotionResult", emotion); // 감정 결과
			result.put("confidence", String.format("%.2f", confidence)); // 감정 %
			result.put("imagePath", imageUrl); // 이미지

			Map<String, Double> probabilities = new HashMap<>();
			root.get("probabilities").fields().forEachRemaining(entry -> {
				probabilities.put(entry.getKey(), entry.getValue().asDouble());
			});
			result.put("probabilities", probabilities);

			// 임시 파일 삭제
			tempFile.delete();

		} catch (Exception e) {
			e.printStackTrace();
			result.put("emotionResult", "error");
			result.put("confidence", "0");
			result.put("imagePath", "");
		}

		return result;
	}

	// 펫 삭제 로직
	@ResponseBody
	@RequestMapping("/usr/pet/delete")
	public ResultData doDelete(HttpServletRequest req, @RequestParam("petId") int petId) {
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return ResultData.from("F-1", "권한이 없습니다.");
		}

		ResultData deleteRd = petService.deletePet(petId);
		int id = rq.getLoginedMemberId();
		return ResultData.from(deleteRd.getResultCode(), deleteRd.getMsg()); // JSP 경로
	}

	// 백신 등록 로직
// 백신 등록
	@RequestMapping("/usr/pet/vaccination/doRegistration")
	@ResponseBody
	public ResultData doRegistration(HttpServletRequest req, @RequestParam("petId") int petId, String vaccineName,
			String injectionDate, String notes) {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return ResultData.from("F-0", "권한이 없습니다.");
		}

		if (Ut.isEmptyOrNull(vaccineName)) {
			return ResultData.from("F-2", "백신 이름을 입력하세요");
		}
		if (Ut.isEmptyOrNull(injectionDate)) {
			return ResultData.from("F-3", "접종 날짜를 입력하세요");
		}

		ResultData rd;

		if (notes == null) {
			rd = petVaccinationService.insertPetVaccination(petId, vaccineName, injectionDate);
		} else {
			rd = petVaccinationService.insertPetVaccinationWithNotes(petId, vaccineName, injectionDate, notes);
		}

		// ✅ 추가: 등록 후 모든 nextDueDate 갱신
		petVaccinationService.updateNextDueDates(petId, vaccineName);

		return rd;
	}

	// 백신 수정
	@RequestMapping("/usr/pet/vaccination/doModify")
	@ResponseBody
	public ResultData doVaccinationModify(@RequestParam("vaccinationId") int vaccinationId,
			@RequestParam String vaccineName, @RequestParam String injectionDate,
			@RequestParam(required = false) String notes) {

		PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
		int petId = petVaccination.getPetId();
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);

		if (pet.getMemberId() != memberId) {
			return ResultData.from("F-0", "권한이 없습니다.");
		}

		if (Ut.isEmptyOrNull(vaccineName)) {
			return ResultData.from("F-1", "백신명을 입력하세요");
		}

		if (Ut.isEmptyOrNull(injectionDate)) {
			return ResultData.from("F-2", "접종일자를 입력하세요");
		}

		ResultData modifyRd;
		if (Ut.isEmptyOrNull(notes)) {
			modifyRd = petVaccinationService.updatePetVaccination(vaccinationId, vaccineName, injectionDate);
		} else {
			modifyRd = petVaccinationService.updatePetVaccinationWithNotes(vaccinationId, vaccineName, injectionDate,
					notes);
		}

		// ✅ 추가: 수정 후 nextDueDate 전체 갱신
		petVaccinationService.updateNextDueDates(petId, vaccineName);

		return ResultData.from("S-1", "수정 완료");
	}

	@RequestMapping("/usr/pet/vaccination/events")
	@ResponseBody
	public List<Map<String, Object>> getVaccinationEvents(@RequestParam("petId") int petId) {
		List<PetVaccination> list = petVaccinationService.getVaccinationsByPetId(petId);
		List<Map<String, Object>> events = new ArrayList<>();

		for (PetVaccination pv : list) {
			Map<String, Object> injEvent = new HashMap<>();
			injEvent.put("id", pv.getId());
			injEvent.put("title", pv.getVaccineName() + " 접종");
			injEvent.put("start", pv.getInjectionDate().toString());
			injEvent.put("notes", pv.getNotes());
			events.add(injEvent);

			if (pv.getNextDueDate() != null) {
				Map<String, Object> nextEvent = new HashMap<>();
				nextEvent.put("id", pv.getId());
				nextEvent.put("title", pv.getVaccineName() + "예정");
				nextEvent.put("start", pv.getNextDueDate().toString());
				nextEvent.put("notes", pv.getNotes());
				events.add(nextEvent);
			}
		}

		return events;
	}

	// 벡신 지우는 로직
	@ResponseBody
	@RequestMapping("/usr/pet/vaccination/delete")
	public ResultData doVaccinationDelete(@RequestParam("vaccinationId") int vaccinationId) {

		PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
		int petId = petVaccination.getPetId();
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);

		if (pet.getMemberId() != memberId) {
			return ResultData.from("F-0", "권한이 없습니다.");
		}

		petVaccinationService.deletePetVaccination(vaccinationId);

		return ResultData.from("S-1", "삭제가 완료되었습니다.");
	}

	@RequestMapping("/usr/pet/vaccination/monthly")
	@ResponseBody
	public List<PetVaccination> getMonthlyVaccinations(@RequestParam int petId, @RequestParam String yearMonth // 예:
																												// "2025-07"
	) {
		return petVaccinationService.getVaccinationsByMonth(petId, yearMonth);
	}

	// 펫 감정일기 페이지 이동
	@RequestMapping("/usr/pet/daily")
	public String showDaily(@RequestParam("petId") int petId, Model model, HttpServletResponse resp)
			throws IOException {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}
		List<CalendarEvent> events = calendarEventService.getEventsByPetId(petId);
		model.addAttribute("events", events); // 감정일기에 등록된 이벤트들
		model.addAttribute("petId", petId); // 해당 펫의 ID
		model.addAttribute("petBirthDate", pet.getBirthDate());
		return "usr/pet/daily";
	}

	@GetMapping("/usr/pet/daily/list")
	@ResponseBody
	public List<Map<String, Object>> getEventList(@RequestParam int petId) {
		List<CalendarEvent> events = calendarEventService.getEventsByPetId(petId);

		return events.stream().map(e -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", e.getId());
			m.put("title", e.getTitle());
			m.put("start", e.getEventDate().toString()); // "2025-07-17"
			m.put("allDay", true);

			Map<String, Object> props = new HashMap<>();
			props.put("content", e.getContent());
			m.put("extendedProps", props);

			return m;
		}).collect(Collectors.toList());
	}

	// 펫 감정일지 등록 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/write")
	@ResponseBody
	public Map<String, Object> addEvent(@RequestParam("petId") int petId,
			@RequestParam("eventDate") String eventDateStr, @RequestParam("title") String title,
			@RequestParam("content") String content, HttpServletRequest req) {

		Map<String, Object> result = new HashMap<>();

		try {
			// 유효성 검사
			if (Ut.isEmptyOrNull(title)) {
				result.put("resultCode", "F-1");
				result.put("msg", "감정을 선택하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(content)) {
				result.put("resultCode", "F-2");
				result.put("msg", "내용을 입력하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(eventDateStr)) {
				result.put("resultCode", "F-3");
				result.put("msg", "날짜를 선택하세요");
				return result;
			}

			// 날짜 파싱
			LocalDate eventDate = LocalDate.parse(eventDateStr);

			// petId로 memberId 추출
			Pet pet = petService.getPetsById(petId);
			if (pet == null) {
				result.put("resultCode", "F-4");
				result.put("msg", "해당 반려동물을 찾을 수 없습니다.");
				return result;
			}

			int loginedMemberId = pet.getMemberId();

			// DB 저장
			ResultData doWriteRd = calendarEventService.insert(loginedMemberId, eventDate, title, petId, content);

			result.put("resultCode", doWriteRd.getResultCode()); // S- 또는 F-
			result.put("msg", doWriteRd.getMsg()); // 오류 메세지 및 성공 메세지
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			result.put("resultCode", "F-500");
			result.put("msg", "서버 오류: " + e.getMessage());
			return result;
		}
	}

	// 감정일지 수정 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/domodify")
	@ResponseBody
	public Map<String, Object> updateEvent(@RequestParam("id") int id, @RequestParam("eventDate") String eventDateStr,
			@RequestParam("title") String title, @RequestParam("content") String content) {

		Map<String, Object> result = new HashMap<>();

		try {
			// 유효성 검사
			if (Ut.isEmptyOrNull(title)) {
				result.put("resultCode", "F-1");
				result.put("msg", "감정을 선택하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(content)) {
				result.put("resultCode", "F-2");
				result.put("msg", "내용을 입력하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(eventDateStr)) {
				result.put("resultCode", "F-3");
				result.put("msg", "날짜를 선택하세요");
				return result;
			}

			// 날짜 파싱
			LocalDate eventDate = LocalDate.parse(eventDateStr);

			// DB 저장
			ResultData doWriteRd = calendarEventService.update(id, eventDate, title, content);

			result.put("resultCode", doWriteRd.getResultCode()); // S- 또는 F-
			result.put("msg", doWriteRd.getMsg()); // 오류 메세지 및 성공 메세지
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			result.put("resultCode", "F-500");
			result.put("msg", "서버 오류: " + e.getMessage());
			return result;
		}
	}

	// 감정일지 삭제 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/delete")
	@ResponseBody
	public Map<String, Object> deleteEvent(@RequestParam("id") int id) {
		Map<String, Object> result = new HashMap<>();

		CalendarEvent calendarEvent = calendarEventService.getEventsById(id);
		if (calendarEvent == null) {
			result.put("resultCode", "F-1");
			result.put("msg", "해당 일기를 찾을 수 없습니다.");
			return result;
		}

		calendarEventService.delete(id);

		result.put("resultCode", "S-1");
		result.put("msg", "삭제가 완료되었습니다.");
		result.put("petId", calendarEvent.getPetId());
		return result;
	}

	// 일 기 상세 보기 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/detail")
	@ResponseBody
	public Map<String, Object> detailEvent(@RequestParam("id") int id) {
		CalendarEvent calendarEvent = calendarEventService.getEventsById(id);

		if (calendarEvent == null) {
			return Map.of("resultCode", "F-1", "msg", "해당 일기를 찾을 수 없습니다.");
		}

		return Map.of("resultCode", "S-1", "calendarEvent", calendarEvent);
	}

	@RequestMapping("/usr/pet/qr")
	public String showQrPage(@RequestParam("petId") int petId, Model model) {
		model.addAttribute("petId", petId);// 없으면 null
		return "usr/pet/qrTest"; // → templates/usr/pet/qr.html
	}

	// 페이지 라우팅: petId만 받아 템플릿 오픈
	@GetMapping("/usr/pet/explain")
	public String showExplainPage(@RequestParam("petId") int petId,
			@RequestParam(value = "docId", required = false) Integer docId, Model model) {
		model.addAttribute("petId", petId);
		model.addAttribute("docId", docId);
		return "usr/pet/explain"; // templates/usr/pet/explain.html
	}

	// 목록 API: lab 문서 + 병원/방문일 포함
	@GetMapping("/usr/pet/lab-docs")
	@ResponseBody
	public Map<String, Object> listLabDocs(@RequestParam int petId, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "100") int pageSize) {
		Map<String, Object> data = medicalDocumentService.getLabDocsByPetId(petId, page, pageSize);
		return Map.of("rows", data.get("rows"), "total", data.get("total"), "page", data.get("page"), "pageSize",
				data.get("pageSize"));
	}

	@GetMapping(value = "/usr/pet/doc/{docId}/structured", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> getStructured(@PathVariable int docId) {
		return composeService.getStructuredByDocId(docId);
	}

	@GetMapping(value = "/usr/pet/doc/{docId}/explain", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> explainMarkdownByDocId(@PathVariable int docId) {
		return composeService.explainMarkdownByDocId(docId);
	}

	@RequestMapping("/usr/pet/convert")
	public String showPages() {
		return "usr/pet/convert"; // → templates/usr/pet/qr.html
	}

	@PostMapping(value = "api/visit/{visitId}/hospital", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> updateVisitHospital(@PathVariable int visitId,
			@RequestBody(required = false) Map<String, String> body) {
		String hospital = (body == null) ? null : body.get("hospital");
		if (hospital != null) {
			hospital = hospital.trim().replaceAll("\\s+", " ");
		}
		if (!StringUtils.hasText(hospital)) {
			return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "병원명은 비워둘 수 없습니다."));
		}
		if (hospital.length() > 100) {
			return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "병원명은 100자 이내로 입력해주세요."));
		}

		try {
			int updated = visitService.updateHospital(visitId, hospital);
			if (updated == 0) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("ok", false, "visitId", visitId, "message", "해당 방문(visit)을 찾을 수 없습니다."));
			}
			return ResponseEntity.ok(Map.of("ok", true, "visitId", visitId, "hospital", hospital));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("ok", false, "visitId", visitId, "message", "저장 중 오류가 발생했습니다."));
		}
	}

}