// ✅ [추가] OCR 전용 컨트롤러
//    - 기능: 업로드된 이미지를 Google Cloud Vision(GCV)으로 OCR 수행 후 React 친화 JSON으로 반환
//    - 특징: 엔드포인트(/api/ocr/extract) 및 응답 포맷은 유지, 내부 OCR 엔진만 GCV로 교체
//    - 준비: (1) pom.xml에 google-cloud-vision 의존성 추가
//            (2) 환경변수 GOOGLE_APPLICATION_CREDENTIALS 로 서비스계정키(JSON) 경로 설정
//            (3) application.yml에 gcv.ocrMode 설정(선택, 기본 DOCUMENT_TEXT_DETECTION)

package com.example.RSW.api;

import java.time.LocalDate;
import java.util.*;

import com.example.RSW.service.LabResultDetailService;
import org.springframework.beans.factory.annotation.Value; // application.yml 설정값 주입
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 업로드 파일 수신

import com.example.RSW.vo.ResultData; // ✅ 프로젝트의 ResultData 경로에 맞게 유지(성공/실패 표준 응답)
import com.example.RSW.vo.OcrSaveVo;
import com.example.RSW.service.MedicalDocumentService;
import com.example.RSW.service.VisitService;
import com.example.RSW.vo.MedicalDocument;
import com.example.RSW.vo.Visit;
import com.fasterxml.jackson.databind.ObjectMapper;

// ⬇️ Google Cloud Vision SDK (GCV) 사용을 위한 임포트
import com.google.cloud.vision.v1.AnnotateImageRequest; // 이미지 요청 객체
import com.google.cloud.vision.v1.AnnotateImageResponse; // 단일 이미지 응답
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;// 배치 응답(여러 이미지)
import com.google.cloud.vision.v1.Feature; // 어떤 기능(TEXT_DETECTION 등) 사용할지
import com.google.cloud.vision.v1.Feature.Type; // Feature 타입 enum
import com.google.cloud.vision.v1.Image; // GCV용 이미지 객체
import com.google.cloud.vision.v1.ImageAnnotatorClient; // GCV 클라이언트
import com.google.protobuf.ByteString; // 바이트 컨테이너
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

// ⬇️ [보존용 주석] Tess4J 기반 사용 시 필요했던 임포트 (현재는 GCV 사용으로 미사용)
// import java.nio.file.Files;
// import java.nio.file.Path;
// import net.sourceforge.tess4j.ITesseract;
// import net.sourceforge.tess4j.Tesseract;

//[추가] React 친화 JSON 파싱/포맷을 위해
import com.example.RSW.service.OcrFormatService;
import com.example.RSW.dto.OcrParseResponse; // ✅ dto 패키지의 DTO
import com.example.RSW.dto.OcrParseResponse.DocType; // ✅ dto 패키지의 공용 enum

//[추가] 멀티파트 consumes 명시용
import org.springframework.http.MediaType;

@RestController // JSON 기반 응답을 반환하는 컨트롤러임을 선언
@RequestMapping("/api/ocr") // 이 컨트롤러의 기본 URL prefix
public class OcrController {

	@Value("${gcv.credentials.json:}")
	private String gcvCredJson;

	@Value("${gcv.credentials.path:}")
	private String gcvCredPath;

	@Value("${gcv.credentials.base64:}")
	private String gcvCredBase64;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired(required = false)
	private Cloudinary cloudinary;

	@Autowired
	private OcrFormatService ocrFormatService;

	// ✅ [보존] Tess4J용 설정(현재 GCV로 전환했지만, 추후 토글 시 재사용 가능)
	@Value("${tesseract.datapath:}") // tessdata 상위 경로(비워두면 OS 기본 경로 사용)
	private String tessDataPath;

	@Value("${tesseract.language:kor+eng}") // Tess4J 언어 설정(한국어+영어)
	private String tessLanguage;

	// ✅ [추가] GCV OCR 모드 주입(없으면 문서형 OCR로 동작)
	// - TEXT_DETECTION: 일반 사진(간판/짧은 글)
	// - DOCUMENT_TEXT_DETECTION: 영수증/문서(표/여러 줄 텍스트) 권장
	@Value("${gcv.ocrMode:DOCUMENT_TEXT_DETECTION}")
	private String gcvOcrMode;

	// [추가] 업로드 파일 저장 루트 (기본값: 프로젝트 루트의 /uploads)
	// application.yml 에서 app.upload.base-dir 로 변경 가능
	@Value("${app.upload.base-dir:uploads}")
	private String baseUploadDir;

	@Autowired
	private LabResultDetailService labResultDetailService;

	// 📌 의료 문서(진단서, 영수증 등) 관련 비즈니스 로직을 처리하는 서비스
	private final MedicalDocumentService medicalDocumentService;
	// 📌 방문(visit) 관련 비즈니스 로직을 처리하는 서비스
	private final VisitService visitService;
	// 📌 JSON 직렬화/역직렬화를 담당하는 Jackson ObjectMapper
	// - LocalDateTime 등 Java 8 날짜/시간 타입 처리 가능 (스프링 빈으로 주입)
	private final ObjectMapper objectMapper;

	/*
	 * 📌 생성자 주입(Constructor Injection) - final 필드(불변성 보장)는 반드시 생성자에서 한 번만 초기화 가능 -
	 * 스프링이 MedicalDocumentService, VisitService, ObjectMapper 빈을 자동 주입
	 */
	public OcrController(MedicalDocumentService medicalDocumentService, VisitService visitService,
			ObjectMapper objectMapper) {
		this.medicalDocumentService = medicalDocumentService;
		this.visitService = visitService;
		this.objectMapper = objectMapper;
	}

	// ✅ [추가] VO 기반 OCR 텍스트 저장
	// - 요청: OcrSaveVo(JSON)
	// - 응답: { resultCode, msg, data: { visitId, documentId } }
	// ✅ LAB 디테일 저장 되도록 camelCase → snake_case 변환 적용 버전
	@PostMapping("/save")
	public ResultData<Map<String, Object>> saveOcrText(@RequestBody OcrSaveVo vo) {
		// 1) 입력 유효성
		boolean hasText = vo.getText() != null && !vo.getText().isBlank();
		boolean hasGroups = vo.getGroups() != null && !vo.getGroups().isEmpty();

		if (!hasText && !hasGroups) {
			return ResultData.from("F-EMPTY", "저장할 그룹/텍스트가 없습니다.", "data", null);
		}
		if (vo.getVisitId() == null && vo.getPetId() == null) {
			return ResultData.from("F-NO-TARGET", "visitId 또는 petId가 필요합니다.", "data", null);
		}

		// 2) 문서유형 결정
		String dt = vo.getDocType();
		if ((dt == null || "auto".equalsIgnoreCase(dt)) && hasGroups) {
			dt = guessDocTypeFromGroups(vo.getGroups()); // groups 기반 1차 추정
		}
		dt = normalizeDocType(dt, hasText ? vo.getText() : null); // 텍스트 기반 정교화
		vo.setDocType(dt);

		// 3) 유형별로 Visit 필드 보강
		if ("diagnosis".equalsIgnoreCase(vo.getDocType()) && hasGroups) {
			hydrateVisitFromDiagnosisGroups(vo);
		}
		if ("receipt".equalsIgnoreCase(vo.getDocType()) && hasGroups) {
			hydrateVisitFromReceiptGroup(vo); // ⚠️ 사용자가 원하는 Notes 포맷 포함(방문이유/진료내역/금액 등)
		}
		if ("lab".equalsIgnoreCase(vo.getDocType()) && hasGroups) {
			hydrateVisitFromLabGroup(vo);
		}

		try {
			// 4) 안전한 docType 보정
			String safeDocType = (vo.getDocType() == null) ? "diagnosis" : vo.getDocType().toLowerCase();
			switch (safeDocType) {
			case "receipt", "prescription", "lab", "diagnosis", "other" -> {
			}
			default -> safeDocType = "diagnosis";
			}

			String safeFileUrl = (vo.getFileUrl() == null || vo.getFileUrl().isBlank()) ? "" : vo.getFileUrl();

			// 5) Visit 생성(없는 경우)
			Integer visitId = vo.getVisitId();
			if (visitId == null) {
				Visit visit = new Visit();
				visit.setTotalCost(vo.getTotalCost());
				visit.setPetId(vo.getPetId());
				visit.setVisitDate(vo.getVisitDate() != null ? vo.getVisitDate() : LocalDateTime.now());
				visit.setHospital(vo.getHospital());
				visit.setDoctor(vo.getDoctor());
				visit.setDiagnosis(vo.getDiagnosis());
				visit.setNotes(vo.getNotes());
				visitId = visitService.insertVisit(visit);
				if (visitId == null || visitId <= 0) {
					throw new IllegalStateException("Visit PK 생성 실패");
				}
			}

			// 6) 저장 payload 구성
			Map<String, Object> payload = new LinkedHashMap<>();
			Map<String, Object> meta = new LinkedHashMap<>();
			meta.put("engine", "gcv");
			meta.put("ts", LocalDateTime.now().toString());
			payload.put("meta", meta);

			// ✅ LAB 의 경우 groups를 snake_case로 변환하여 저장 (lab_result_detail 파서 호환)
			List<Map<String, Object>> groupsToPersist = vo.getGroups();
			if (hasGroups && "lab".equalsIgnoreCase(safeDocType)) {
				groupsToPersist = normalizeLabGroupsForPersistence(groupsToPersist); // ← 헬퍼 메서드 필요
			}

			if (hasGroups)
				payload.put("groups", groupsToPersist);
			else
				payload.put("text", vo.getText().trim());

			String ocrJson = objectMapper.writeValueAsString(payload);

			// 7) MedicalDocument 저장
			MedicalDocument doc = new MedicalDocument();
			doc.setVisitId(visitId);
			doc.setDocType(safeDocType);
			doc.setFileUrl(safeFileUrl);
			doc.setOcrJson(ocrJson);
			int documentId = medicalDocumentService.insertDocument(doc);

			// 8) LAB이면 디테일 테이블에 펼쳐 저장
			int labRows = 0;
			if ("lab".equalsIgnoreCase(safeDocType)) {
				try {
					labRows = labResultDetailService.upsertLatestGroup(documentId, ocrJson);
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
			}

			// 9) 응답
			Map<String, Object> data = new HashMap<>();
			data.put("visitId", visitId);
			data.put("documentId", documentId);
			data.put("fileUrl", vo.getFileUrl());
			data.put("labRows", labRows);
			return ResultData.from("S-OCR-SAVE", "OCR 데이터가 저장되었습니다.", "data", data);

		} catch (Exception e) {
			Map<String, Object> err = new HashMap<>();
			err.put("errorType", e.getClass().getSimpleName());
			err.put("error", e.getMessage());
			return ResultData.from("F-OCR-SAVE", "OCR 저장 중 오류", "data", err);
		}
	}

	@SuppressWarnings("unchecked")
	private void hydrateVisitFromLabGroup(OcrSaveVo vo) {
		if (vo.getGroups() == null || vo.getGroups().isEmpty())
			return;

		Map<String, Object> best = pickBestLabGroup(vo.getGroups());
		if (best == null)
			return;

		// date → Visit.visitDate (vo에 이미 값이 없을 때만)
		if (vo.getVisitDate() == null) {
			LocalDateTime dt = tryParseToLdt(String.valueOf(best.getOrDefault("date", "")));
			if (dt != null)
				vo.setVisitDate(dt);
		}

		// 병원명 보강 (hospital/store 키 우선)
		if (vo.getHospital() == null || vo.getHospital().isBlank()) {
			Map<String, Object> kv = findKv(best, "hospital", "store", "병원명", "기관명");
			if (kv != null)
				vo.setHospital(String.valueOf(kv.get("value")));
		}
	}

	/** 최신 날짜 + 실제 검사값이 있는 그룹을 우선 선택 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> pickBestLabGroup(List<?> groups) {
		Map<String, Object> best = null;
		long bestScore = Long.MIN_VALUE;
		for (Object o : groups) {
			if (!(o instanceof Map))
				continue;
			Map<String, Object> g = (Map<String, Object>) o;
			String date = String.valueOf(g.getOrDefault("date", ""));
			long ts = toEpochSafe(date);

			// name/value 형태의 검사값이 1개라도 있으면 가중치
			boolean hasValues = false;
			Object items = g.get("items");
			if (items instanceof List) {
				for (Object it : (List<?>) items) {
					if (it instanceof Map) {
						Map<String, Object> m = (Map<String, Object>) it;
						if (m.containsKey("name") && m.get("value") != null) {
							hasValues = true;
							break;
						}
					}
				}
			}
			long score = ts + (hasValues ? 1_000_000_000_000L : 0L);
			if (score > bestScore) {
				bestScore = score;
				best = g;
			}
		}
		return best;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> findKv(Map<String, Object> group, String... keys) {
		Object items = group.get("items");
		if (!(items instanceof List))
			return null;
		for (Object it : (List<?>) items) {
			if (!(it instanceof Map))
				continue;
			Map<String, Object> m = (Map<String, Object>) it;
			if (!m.containsKey("key"))
				continue;
			String k = String.valueOf(m.get("key"));
			for (String want : keys) {
				if (want.equalsIgnoreCase(k))
					return m;
			}
		}
		return null;
	}

	private LocalDateTime tryParseToLdt(String s) {
		if (s == null || s.isBlank() || "Unknown".equalsIgnoreCase(s))
			return null;
		try {
			String t = s.trim();
			if (t.length() > 10)
				return LocalDateTime.parse(t.replace(' ', 'T'));
			return LocalDate.parse(t).atStartOfDay();
		} catch (Exception e) {
			return null;
		}
	}

	private long toEpochSafe(String s) {
		try {
			LocalDateTime dt = tryParseToLdt(s);
			return (dt == null) ? Long.MIN_VALUE / 2 : dt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
		} catch (Exception e) {
			return Long.MIN_VALUE / 2;
		}
	}

	private void hydrateVisitFromDiagnosisGroups(OcrSaveVo vo) {
		if (vo.getGroups() == null || vo.getGroups().isEmpty())
			return;

		// 가장 알찬 그룹 하나 선택
		Map<String, Object> best = null;
		for (var g : vo.getGroups()) {
			Object items = g.get("items");
			if (items instanceof java.util.List<?> list && !list.isEmpty()) {
				best = g;
				break;
			}
		}
		if (best == null)
			return;

		// items -> Map<String,String>
		Map<String, String> kv = new LinkedHashMap<>();
		Object items = best.get("items");
		if (items instanceof java.util.List) {
			for (Object o : (java.util.List<?>) items) {
				if (!(o instanceof java.util.Map))
					continue;

				@SuppressWarnings("unchecked")
				java.util.Map<?, ?> mm = (java.util.Map<?, ?>) o;

				String key = java.util.Objects.toString(mm.get("key"), "").trim();
				String val = java.util.Objects.toString(mm.get("value"), "").trim();

				if (key.isEmpty() || val.isEmpty())
					continue;
				kv.put(key.toLowerCase(java.util.Locale.ROOT), val);
			}
		}

		// 병원/의사/진단
		if (kv.containsKey("hospital"))
			vo.setHospital(kv.get("hospital"));
		if (kv.containsKey("doctor"))
			vo.setDoctor(kv.get("doctor"));
		if (kv.containsKey("diagnosis"))
			vo.setDiagnosis(kv.get("diagnosis"));

		// ✅ notes: prognosis + others + (option) therapy
		List<String> notesBits = new ArrayList<>();
		if (kv.containsKey("prognosis"))
			notesBits.add("예후: " + kv.get("prognosis"));
		if (kv.containsKey("others"))
			notesBits.add("기타: " + kv.get("others"));
		if (kv.containsKey("therapy"))
			notesBits.add("치료: " + kv.get("therapy"));
		if (!notesBits.isEmpty())
			vo.setNotes(String.join(" · ", notesBits));

		// ✅ visitDate: diagnosisDate > onsetDate > group.date (YYYY-MM-DD)
		String d = kv.get("diagnosisdate");
		if (d == null || d.isBlank())
			d = kv.get("onsetdate");
		if ((d == null || d.isBlank()) && best.get("date") instanceof String ds)
			d = ds;

		if (d != null && !d.isBlank()) {
			try {
				java.time.LocalDate dd = (d.length() > 10) ? java.time.LocalDate.parse(d.substring(0, 10))
						: java.time.LocalDate.parse(d);
				vo.setVisitDate(dd.atStartOfDay()); // 시간 없으니 00:00:00
			} catch (Exception ignore) {
				/* 파싱 실패 시 그냥 패스 */ }
		}
	}

	@SuppressWarnings("unchecked")
	private void hydrateVisitFromReceiptGroup(OcrSaveVo vo) {
		if (vo.getGroups() == null || vo.getGroups().isEmpty())
			return;

		// 1) 가장 알찬 그룹(라인아이템 많은 것) 고르기
		Map<String, Object> best = null;
		int bestLines = -1;
		for (var g : vo.getGroups()) {
			Object items = g.get("items");
			if (!(items instanceof java.util.List<?> list))
				continue;
			int lineCnt = 0;
			for (Object o : list) {
				if (o instanceof java.util.Map<?, ?> mm) {
					Object t = ((java.util.Map<?, ?>) o).get("type");
					if ("line".equals(t))
						lineCnt++;
				}
			}
			if (lineCnt > bestLines) {
				bestLines = lineCnt;
				best = g;
			}
			if (best == null)
				best = g; // 폴백
		}
		if (best == null)
			return;

		// 2) items → key/value 맵(소문자 키)
		java.util.Map<String, String> kv = new java.util.LinkedHashMap<>();
		java.util.List<?> items = (java.util.List<?>) best.get("items");
		if (items != null) {
			for (Object o : items) {
				if (!(o instanceof java.util.Map<?, ?> mm))
					continue;
				Object key = mm.get("key");
				Object val = mm.get("value");
				if (key != null && val != null) {
					String k = String.valueOf(key).trim().toLowerCase(java.util.Locale.ROOT);
					String v = String.valueOf(val).trim();
					if (!k.isEmpty() && !v.isEmpty())
						kv.put(k, v);
				}
			}
		}

		// 3) 병원명 보강 (store/hospital)
		if (nz(vo.getHospital()) == null) {
			String store = kv.get("store");
			String hosp = kv.get("hospital");
			if (nz(store) != null)
				vo.setHospital(store);
			else if (nz(hosp) != null)
				vo.setHospital(hosp);
		}

		// 4) visitDate: 그룹의 date 우선(yyyy-MM-dd 또는 yyyy-MM-dd HH:mm:ss)
		String ds = (best.get("date") instanceof String) ? (String) best.get("date") : null;
		if (ds != null && !ds.isBlank()) {
			try {
				java.time.LocalDateTime dt = (ds.length() > 10) ? java.time.LocalDateTime.parse(ds.replace(' ', 'T'))
						: java.time.LocalDate.parse(ds).atStartOfDay();
				vo.setVisitDate(dt);
			} catch (Exception ignore) {
			}
		}

		// 5) 노트 구성 요구사항:
		// "방문이유: {reason}, 진료 내역: {item1, item2}, 금액: {total}원"
		// - reason: 그룹의 reason 필드 > kv.reason/symptom/증상 등
		String reason = null;
		Object reasonObj = best.get("reason");
		if (reasonObj != null) {
			String r = String.valueOf(reasonObj).trim();
			if (!r.isEmpty())
				reason = r;
		}
		if (reason == null) {
			if (kv.get("reason") != null)
				reason = kv.get("reason");
			else if (kv.get("symptom") != null)
				reason = kv.get("symptom");
			else if (kv.get("chief complaint") != null)
				reason = kv.get("chief complaint");
			else if (kv.get("증상") != null)
				reason = kv.get("증상");
		}

		// - 진료 내역: line 항목들의 item 이름만 추림
		java.util.List<String> names = new java.util.ArrayList<>();
		if (items != null) {
			for (Object o : items) {
				if (!(o instanceof java.util.Map<?, ?> mm))
					continue;
				if (!"line".equals(String.valueOf(mm.get("type"))))
					continue;
				String item = nz(mm.get("item"));
				if (item != null)
					names.add(item);
			}
		}

		// - 금액: summary.total(> subtotal 폴백)
		Long total = null;
		if (kv.get("total") != null)
			total = toLong(kv.get("total"));
		if (total == null)
			total = toLong(kv.get("subtotal"));

		// - VO의 totalCost 세팅(타입이 무엇이든 반영)
		if (total != null) {
			try {
				vo.getClass().getMethod("setTotalCost", java.math.BigDecimal.class).invoke(vo,
						new java.math.BigDecimal(total));
			} catch (Throwable ___) {
				try {
					vo.getClass().getMethod("setTotalCost", Integer.class).invoke(vo, total.intValue());
				} catch (Throwable ____) {
					try {
						vo.getClass().getMethod("setTotalCost", Long.class).invoke(vo, total.longValue());
					} catch (Throwable _____) {
						/* ignore */ }
				}
			}
		}

		// - 문자열 조합
		java.util.List<String> bits = new java.util.ArrayList<>();
		if (reason != null)
			bits.add("방문이유: " + reason);
		if (!names.isEmpty())
			bits.add("진료 내역: " + String.join(", ", names));
		if (total != null)
			bits.add("금액: " + total + "원"); // 천단위 구분 없이 원문대로

		String notes = String.join(", ", bits);
		if (!notes.isBlank())
			vo.setNotes(notes);

		// 6) 구분용 진단 텍스트(없으면 "영수증")
		if (nz(vo.getDiagnosis()) == null)
			vo.setDiagnosis("영수증");
	}

	// 작은 헬퍼
	private static String nz(Object o) {
		if (o == null)
			return null;
		String s = String.valueOf(o).trim();
		return s.isEmpty() ? null : s;
	}

	private static Long toLong(Object o) {
		if (o == null)
			return null;
		try {
			String s = String.valueOf(o).replaceAll("[^\\d]", "");
			return s.isEmpty() ? null : Long.valueOf(s);
		} catch (Exception e) {
			return null;
		}
	}

	// [추가 - 클래스 내부 아무 곳(메서드 아래 추천)]
	/** 원본 이미지를 저장하고 /files/**로 접근 가능한 URL을 반환한다. */
	/**
	 * ✅ 원본 이미지를 Cloudinary로 저장하고 URL을 반환한다. - Cloudinary 빈이 없거나 업로드 실패 시, 기존 로컬
	 * 저장으로 폴백한다. - 반환: https://res.cloudinary.com/... 형태(Cloudinary) 또는 /files/...
	 * (로컬)
	 */
	// ✅ Cloudinary 우선 업로드 + 실패 시 로컬 폴백
	private String saveFileAndReturnUrl(byte[] bytes, String originalFilename) throws java.io.IOException {
		// ⛳ 날짜 기반 경로 (Cloudinary 폴더/로컬 폴더 공통)
		String yyyy = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy"));
		String mm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MM"));
		String dd = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd"));
		String folder = "ocr/" + yyyy + "/" + mm + "/" + dd;

		// 🔒 기본 유효성
		if (bytes == null || bytes.length == 0) {
			throw new IllegalArgumentException("빈 파일 바이트입니다.");
		}

		// 1) Cloudinary 우선 시도
		if (cloudinary != null) {
			try {
				// 고유 public_id 생성 (확장자는 Cloudinary가 처리)
				String publicId = java.util.UUID.randomUUID().toString().replace("-", "");

				// ✅ 변경점
				// - resource_type: "auto" 로 설정하여 jpg/png/webp/heic/pdf 등 자동 감지
				// - context: 원본 파일명 기록(추후 관리용)
				// - overwrite: false (중복 방지), unique_filename: false (우리가 준 public_id 그대로 사용)
				@SuppressWarnings("unchecked")
				java.util.Map<String, Object> options = com.cloudinary.utils.ObjectUtils.asMap("folder", folder,
						"public_id", publicId, "overwrite", false, "resource_type", "auto", // ✅ 변경: 확장자 무관 자동 감지
						"unique_filename", false, "use_filename", false, "invalidate", true, "context",
						com.cloudinary.utils.ObjectUtils.asMap("original_filename",
								(originalFilename == null ? "" : originalFilename)));

				@SuppressWarnings("unchecked")
				java.util.Map<String, Object> res = cloudinary.uploader().upload(bytes, options);

				Object secureUrl = res.get("secure_url"); // HTTPS URL
				if (secureUrl instanceof String && !((String) secureUrl).isBlank()) {
					return secureUrl.toString(); // 예: https://res.cloudinary.com/...
				}
				// secure_url 미존재 시 폴백
				throw new IllegalStateException("Cloudinary 업로드 응답에 secure_url이 없습니다.");
			} catch (Exception ce) {
				// 업로드 실패 시 폴백으로 진행 (로그만 남김)
				ce.printStackTrace();
			}
		}

		// 2) Cloudinary를 사용할 수 없거나 실패한 경우 → 로컬 저장 폴백
		return saveLocallyAndReturnUrl(bytes, originalFilename, yyyy, mm, dd);
	}

	// ✅ [추가] Cloudinary 실패 시 로컬로 저장하는 폴백 메서드
//  - saveFileAndReturnUrl(...) 안에서 호출됩니다.
//  - 시그니처(인자 5개)가 호출부와 반드시 같아야 합니다.
	private String saveLocallyAndReturnUrl(byte[] bytes, String originalFilename, String yyyy, String mm, String dd)
			throws java.io.IOException {
		// 날짜 기반 폴더(uploads/ocr/yyyy/MM/dd/) 생성
		java.nio.file.Path saveDir = java.nio.file.Paths.get(baseUploadDir, "ocr", yyyy, mm, dd);
		java.nio.file.Files.createDirectories(saveDir);

		// 파일명: UUID + 원본 확장자
		String ext = org.springframework.util.StringUtils.getFilenameExtension(originalFilename);
		String fname = java.util.UUID.randomUUID().toString().replace("-", "");
		if (ext != null && !ext.isBlank()) {
			fname += "." + ext.toLowerCase();
		}

		// 실제 저장
		java.nio.file.Path dest = saveDir.resolve(fname);
		java.nio.file.Files.write(dest, bytes);

		// 정적 리소스 핸들러(/files/**)로 접근 가능한 URL 반환 (WebConfig 매핑 필요)
		return "/files/ocr/" + yyyy + "/" + mm + "/" + dd + "/" + fname;
	}

	/**
	 * ✅ 영수증 이미지에서 텍스트를 추출하는 엔드포인트 - 요청: multipart/form-data; 필드명 "file" 에 이미지 파일 첨부
	 * - 응답: ResultData 표준 포맷 { "resultCode": "S-OK", "msg": "OCR 완료", "data": {
	 * "text": "...", "confidence": null } } ※ confidence는 GCV 기본 응답에 평균 신뢰도가 별도
	 * 제공되지 않아 null로 둠(필요 시 확장)
	 */
	// ✅ 멀티파트 명시(consumes) 추가
	@PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // POST /api/ocr/extract
	public ResultData<Map<String, Object>> extract(@RequestParam("file") MultipartFile file) {
		try {
			// 🔧 [추가] 업로드 파일 유효성 검사 (빈 파일 방지)
			if (file == null || file.isEmpty()) {
				return ResultData.from("F-OCR", "업로드된 파일이 없습니다.");
			}

			// --------------------------------------------------------------------
			// ⛔ [보존용 주석] Tess4J 호출 흐름 (현재 미사용. 향후 엔진 토글 시 참고)
			// Path tmp = Files.createTempFile("ocr_", "_" + file.getOriginalFilename()); //
			// 업로드 파일 임시 저장
			// Files.write(tmp, file.getBytes()); // 바이트 기록
			// ITesseract tess = new Tesseract(); // Tess4J 엔진 생성
			// if (tessDataPath != null && !tessDataPath.isBlank()) {
			// tess.setDatapath(tessDataPath); // tessdata 상위 경로 설정
			// }
			// tess.setLanguage(tessLanguage); // 언어 설정 (kor+eng)
			// String text = tess.doOCR(tmp.toFile()); // OCR 수행 → 전체 텍스트
			// --------------------------------------------------------------------

			// ✅ [실사용] Google Cloud Vision OCR 호출부
			// 1) 업로드 파일 바이트를 읽어 GCV Image 객체로 변환
			// [수정] 스트림을 두 번 읽지 않도록 바이트를 한 번만 확보
			byte[] bytes = file.getBytes();

			// [추가] 원본 이미지 저장 → 접근 URL 생성 (Cloudinary 우선, 실패 시 로컬 폴백)
			String fileUrl = saveFileAndReturnUrl(bytes, file.getOriginalFilename());

			// [수정] GCV 바이트 입력 변경 (readFrom → copyFrom)
			ByteString imgBytes = ByteString.copyFrom(bytes);
			Image img = Image.newBuilder().setContent(imgBytes).build();

			// 2) OCR 모드 결정: 기본은 DOCUMENT_TEXT_DETECTION(영수증/문서에 유리)
			Type type = "TEXT_DETECTION".equalsIgnoreCase(gcvOcrMode) ? Feature.Type.TEXT_DETECTION
					: Feature.Type.DOCUMENT_TEXT_DETECTION;

			// 3) 어떤 기능(Feature)을 사용할지 지정하고 요청 객체 구성
			Feature feat = Feature.newBuilder().setType(type) // 선택한 OCR 모드 지정
					.build();
			AnnotateImageRequest req = AnnotateImageRequest.newBuilder().addFeatures(feat) // 기능 추가
					.setImage(img) // 대상 이미지 설정
					.build(); // 요청 객체 완성

			String text; // 최종 추출 텍스트를 담을 변수

			// 4) GCV 클라이언트를 생성해 배치 요청 실행(여러 장도 가능하나 여기선 1장만)
			// 🔧 [변경] gcv.credentials.* 우선 사용 → 없으면 ADC로 폴백
			Credentials creds = null;

			// 1) JSON 문자열 우선
			if (gcvCredJson != null && !gcvCredJson.isBlank()) {
				try (var in = new ByteArrayInputStream(gcvCredJson.getBytes(StandardCharsets.UTF_8))) {
					creds = ServiceAccountCredentials.fromStream(in); // 서비스계정 JSON 파싱
				}
			}
			// 2) BASE64 문자열
			else if (gcvCredBase64 != null && !gcvCredBase64.isBlank()) {
				byte[] decoded = java.util.Base64.getDecoder().decode(gcvCredBase64);
				try (var in = new ByteArrayInputStream(decoded)) {
					creds = ServiceAccountCredentials.fromStream(in);
				}
			}
			// 3) 경로(classpath:/ 또는 file:/)
			else if (gcvCredPath != null && !gcvCredPath.isBlank()) {
				Resource r = resourceLoader.getResource(gcvCredPath);
				try (var in = r.getInputStream()) {
					creds = ServiceAccountCredentials.fromStream(in);
				}
			}
			// 4) 마지막 수단: ADC(환경변수 GOOGLE_APPLICATION_CREDENTIALS)
			else {
				creds = GoogleCredentials.getApplicationDefault()
						.createScoped("https://www.googleapis.com/auth/cloud-platform");
			}
			// 🔒 [추가] scope 보정: 일부 환경에서 Vision 호출 실패 예방
			if (creds instanceof ServiceAccountCredentials) {
				creds = ((ServiceAccountCredentials) creds)
						.createScoped("https://www.googleapis.com/auth/cloud-platform");
			}

			ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
					.setCredentialsProvider(FixedCredentialsProvider.create(creds)).build();

			try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
				BatchAnnotateImagesResponse resp = client.batchAnnotateImages(java.util.List.of(req));
				AnnotateImageResponse r = resp.getResponses(0);

				if (r.hasError()) {
					throw new IllegalStateException("Vision OCR error: " + r.getError().getMessage());
				}

				if (r.hasFullTextAnnotation()) {
					text = r.getFullTextAnnotation().getText();
				} else if (!r.getTextAnnotationsList().isEmpty()) {
					text = r.getTextAnnotations(0).getDescription();
				} else {
					text = "";
				}
			}

			// ✅ 자동 판별 결과(문자열 + 점수)
			Map<String, Object> guess = suggestDocTypeWithConfidence(text);

			// ✅ [추가] React 친화 JSON 파싱(날짜별 그룹/타입/ASCII) - 서비스 호출
			DocType enumHint = toDocType((String) guess.get("type"));
			OcrParseResponse parsed = ocrFormatService.format(text, enumHint);

			// 7) React에서 다루기 쉬운 JSON 스키마로 가공 (text + confidence + parsed)
			Map<String, Object> payload = new HashMap<>();
			payload.put("text", text != null ? text.trim() : ""); // 전체 텍스트(앞뒤 공백 정리)
			payload.put("confidence", null); // 평균 신뢰도는 별도 계산 시 확장 가능
			payload.put("mode", type.name()); // 사용한 OCR 모드 확인용
			payload.put("fileUrl", fileUrl); // [추가] 프론트가 저장 시 같이 넘길 URL
			payload.put("storage", fileUrl.startsWith("http") ? "cloudinary" : "local");
			payload.put("suggestedDocType", (String) guess.get("type"));
			payload.put("suggestedConfidence", guess.get("confidence"));

			// ✅ [추가] 파싱 결과 포함 (프론트가 즉시 사용 가능)
			payload.put("docType", parsed.getDocType().name().toLowerCase()); // enum
																				// (RECEIPT/PRESCRIPTION/LAB/DIAGNOSIS/UNKNOWN)
			payload.put("groups", parsed.getGroups()); // [{ date, items[] }]
			payload.put("ascii", parsed.getAscii()); // 사람이 보기 쉬운 요약(옵션)

			// 8) 표준 성공 응답(ResultData)로 감싸서 반환
			return ResultData.from("S-OK", "OCR 완료", "data", payload);

		} catch (Exception e) {
			// 9) 예외 발생 시 스택트레이스 로깅 후 표준 실패 응답 반환
			e.printStackTrace();

			// 🔧 [추가] 실패 원인(간단)도 data에 포함 → Network 탭에서 즉시 확인 가능
			Map<String, Object> extra = new HashMap<>();
			extra.put("errorType", e.getClass().getSimpleName());
			extra.put("error", String.valueOf(e.getMessage()));
			extra.put("ocrMode", gcvOcrMode); // [선택] 디버그용
			extra.put("fileSize", (file != null ? file.getSize() : -1)); // [선택] 디버그용

			return ResultData.from("F-OCR", "OCR 처리 중 오류가 발생했습니다.", "data", extra);
		}
	}

	// [추가] 단건 조회: documentId 또는 visitId(해당 방문의 최신 문서)
	@GetMapping("/doc")
	public ResultData<Map<String, Object>> getDoc(
			@RequestParam(value = "documentId", required = false) Integer documentId,
			@RequestParam(value = "visitId", required = false) Integer visitId) {
		try {
			if (documentId == null && visitId == null) {
				return ResultData.from("F-BAD-REQ", "documentId 또는 visitId가 필요합니다.", "data", null);
			}
			MedicalDocument doc = (documentId != null) ? medicalDocumentService.findById(documentId)
					: medicalDocumentService.findLatestByVisitId(visitId);
			if (doc == null) {
				return ResultData.from("F-NOT-FOUND", "문서를 찾을 수 없습니다.", "data", null);
			}

			String text = null;
			Map<String, Object> ocrMeta = new java.util.HashMap<>();
			java.util.List<java.util.Map<String, Object>> groups = null;

			try {
				String json = (doc.getOcrJson() == null || doc.getOcrJson().isBlank()) ? "{}" : doc.getOcrJson();
				com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);

				// text
				text = root.path("text").asText(null);

				// meta
				com.fasterxml.jackson.databind.JsonNode meta = root.path("meta");
				if (meta != null && meta.isObject()) {
					if (meta.hasNonNull("engine"))
						ocrMeta.put("engine", meta.get("engine").asText());
					if (meta.hasNonNull("ts"))
						ocrMeta.put("ts", meta.get("ts").asText());
				}

				// ✅ groups
				com.fasterxml.jackson.databind.JsonNode gnode = root.path("groups");
				if (gnode != null && gnode.isArray()) {
					groups = objectMapper.convertValue(gnode,
							new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {
							});
				}
			} catch (Exception ignore) {
			}

			String storage = (doc.getFileUrl() != null && doc.getFileUrl().startsWith("http")) ? "cloudinary" : "local";
			Map<String, Object> guess = suggestDocTypeWithConfidence(text);

			Map<String, Object> out = new java.util.HashMap<>();
			out.put("documentId", doc.getId());
			out.put("visitId", doc.getVisitId());
			out.put("docType", doc.getDocType());
			out.put("fileUrl", doc.getFileUrl());
			out.put("storage", storage);
			out.put("ocrMeta", ocrMeta);
			out.put("text", text); // 하위호환
			out.put("groups", groups); // ✅ 신규
			out.put("createdAt", doc.getCreatedAt());
			out.put("suggestedDocType", (String) guess.get("type"));
			out.put("suggestedConfidence", guess.get("confidence"));
			return ResultData.from("S-OK", "문서 조회 성공", "data", out);

		} catch (Exception e) {
			Map<String, Object> err = new java.util.HashMap<>();
			err.put("errorType", e.getClass().getSimpleName());
			err.put("error", e.getMessage());
			return ResultData.from("F-ERROR", "문서 조회 중 오류가 발생했습니다.", "data", err);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> normalizeLabGroupsForPersistence(List<Map<String, Object>> groups) {
		if (groups == null)
			return java.util.Collections.emptyList();
		List<Map<String, Object>> outGroups = new ArrayList<>();
		for (Object gObj : groups) {
			if (!(gObj instanceof Map))
				continue;
			Map<String, Object> g = (Map<String, Object>) gObj;
			Map<String, Object> newGroup = new LinkedHashMap<>();
			newGroup.put("date", g.get("date"));

			List<Map<String, Object>> newItems = new ArrayList<>();
			Object itemsObj = g.get("items");
			if (itemsObj instanceof List<?> list) {
				for (Object itObj : list) {
					if (!(itObj instanceof Map))
						continue;
					Map<String, Object> it = (Map<String, Object>) itObj;

					Map<String, Object> row = new LinkedHashMap<>();
					Object name = it.get("name");
					if (name == null)
						name = it.get("testName");
					Object val = it.get("value");
					if (val == null)
						val = it.get("resultValue");
					Object rlo = it.get("ref_low");
					if (rlo == null)
						rlo = it.get("refLow");
					Object rhi = it.get("ref_high");
					if (rhi == null)
						rhi = it.get("refHigh");

					row.put("name", name);
					row.put("value", val);
					row.put("unit", it.get("unit"));
					row.put("ref_low", rlo);
					row.put("ref_high", rhi);
					row.put("flag", it.get("flag"));
					row.put("notes", it.get("notes"));
					newItems.add(row);
				}
			}
			newGroup.put("items", newItems);
			outGroups.add(newGroup);
		}
		return outGroups;
	}

	// ✅ [추가] suggestedDocType(문자열) → OcrParseResponse.DocType(enum) 변환
	// ⛳ 위치: 컨트롤러 "클래스 내부"에 반드시 넣으세요. (extract() '아래'에 배치 완료)
	private DocType toDocType(String s) {
		if (s == null)
			return DocType.UNKNOWN;
		switch (s.toLowerCase()) {
		case "receipt":
			return DocType.RECEIPT;
		case "prescription":
			return DocType.PRESCRIPTION;
		case "lab":
			return DocType.LAB;
		case "diagnosis":
			return DocType.DIAGNOSIS;
		default:
			return DocType.UNKNOWN;
		}
	}

	// ✅ 4종(영수증/처방전/검사결과지/진단서) 자동 분류 + 신뢰도 계산
//  반환: type ∈ {receipt, prescription, lab, diagnosis}, confidence(0~1)
//  디버깅용 점수도 함께 반환(receiptScore, prescriptionScore, labScore, diagnosisScore)
	private Map<String, Object> suggestDocTypeWithConfidence(String text) {
		Map<String, Object> out = new HashMap<>();
		if (text == null || text.isBlank()) {
			out.put("type", "diagnosis"); // 텍스트가 전혀 없으면 무난히 진단서로 가정
			out.put("confidence", 0.5);
			out.put("receiptScore", 0);
			out.put("prescriptionScore", 0);
			out.put("labScore", 0);
			out.put("diagnosisScore", 0);
			return out;
		}

		String t = text.toLowerCase();

		// ── 1) 키워드 세트 ───────────────────────────────────────────────
		String[] receiptHints = { "과세", "면세", "합계", "청구 총액", "총액", "원금액", "vat", "부가세", "영수증", "단가", "수량", "금액", "청구",
				"거래명세서", "승인번호", "신용카드", "현금영수증", "카드사" };
		String[] diagnosisHints = { "진단서", "환자명", "등록번호", "생년월일", "성별", "보호자", "의사", "면허", "면허번호", "병명", "진단명", "소견",
				"의학적 소견", "발급일", "발행일", "의료기관", "직인" };
		String[] labHints = { // Chemistry/CBC/UA 등 공통
				"검사결과", "정상범위", "reference range", "chemistry", "cbc", "urinalysis", "wbc", "rbc", "hgb", "hct", "plt",
				"glucose", "alt", "ast", "alp", "ggt", "bun", "crea", "creatinine", "albumin", "globulin",
				"cholesterol", "triglyceride", "sdma", "lactate", "phos", "calcium", "phosphorus", "anion gap" };
		String[] prescriptionHints = { "처방전", "처방", "복용", "용법", "용량", "투약", "mg", "ml", "tablet", "tab", "cap", "정",
				"캡슐", "1일", "1회", "bid", "tid", "qid", "sid", "q12h", "q24h", "q8h", "q6h", "po", "p.o", "prn", "아침",
				"점심", "저녁", "취침전", "일분량", "일수", "일간" };

		int sReceipt = 0, sDiag = 0, sLab = 0, sRx = 0;

		for (String k : receiptHints)
			if (t.contains(k))
				sReceipt += 2;
		for (String k : diagnosisHints)
			if (t.contains(k))
				sDiag += 2;
		for (String k : labHints)
			if (t.contains(k))
				sLab += 2;
		for (String k : prescriptionHints)
			if (t.contains(k))
				sRx += 2;

		// ── 2) 패턴 가산점 ───────────────────────────────────────────────
		// (a) 금액/천단위 → 영수증
		java.util.regex.Pattern pAmount = java.util.regex.Pattern.compile("\\b\\d{1,3}(?:[.,]\\d{3})+(?:\\s*원)?\\b");
		java.util.regex.Matcher mAmount = pAmount.matcher(text);
		int amountHits = 0;
		while (mAmount.find())
			amountHits++;
		sReceipt += Math.min(amountHits, 6);

		// (b) 단위 → 검사결과지(단위가 많이 등장)
		java.util.regex.Pattern pUnits = java.util.regex.Pattern
				.compile("(?i)(mg/dl|g/dl|mmol/l|µg/dl|ug/dl|ng/ml|u/l|iu/l|pmol/l|k/µl|10\\^\\d+/l|%)");
		java.util.regex.Matcher mUnits = pUnits.matcher(text);
		int unitHits = 0;
		while (mUnits.find())
			unitHits++;
		sLab += Math.min(unitHits, 8);

		// (c) 범위표기 → 검사결과지
		java.util.regex.Pattern pRange = java.util.regex.Pattern
				.compile("\\b\\d+(?:\\.\\d+)?\\s*[-~–]\\s*\\d+(?:\\.\\d+)?\\b");
		java.util.regex.Matcher mRange = pRange.matcher(text);
		int rangeHits = 0;
		while (mRange.find())
			rangeHits++;
		sLab += Math.min(rangeHits, 6);

		// (d) 복약 스케줄(1일2회, q12h 등) → 처방전
		java.util.regex.Pattern pSched = java.util.regex.Pattern
				.compile("(?i)(1\\s*일\\s*\\d+\\s*회|q(?:6|8|12|24)h|bid|tid|qid|sid|po|p\\.o|prn)");
		java.util.regex.Matcher mSched = pSched.matcher(t);
		int schedHits = 0;
		while (mSched.find())
			schedHits++;
		sRx += Math.min(schedHits * 2, 8);

		// ── 3) 최종 결정 ─────────────────────────────────────────────────
		int total = sReceipt + sDiag + sLab + sRx;
		int maxScore = Math.max(Math.max(sReceipt, sDiag), Math.max(sLab, sRx));

		String type;
		if (maxScore == 0)
			type = "diagnosis"; // 완전 모호하면 진단서로 기본값
		else if (maxScore == sReceipt)
			type = "receipt";
		else if (maxScore == sRx)
			type = "prescription";
		else if (maxScore == sLab)
			type = "lab";
		else
			type = "diagnosis";

		double conf = (total == 0) ? 0.5 : (1.0 * maxScore) / total;
		if (conf < 0.5)
			conf = 0.5;

		out.put("type", type);
		out.put("confidence", Math.round(conf * 100) / 100.0);
		out.put("receiptScore", sReceipt);
		out.put("prescriptionScore", sRx);
		out.put("labScore", sLab);
		out.put("diagnosisScore", sDiag);
		return out;
	}

	// [추가] OCR 텍스트 + 원본 파일 동시 저장 (폴백용)
	// 주석: 기존 코드는 유지하고, 아래 메서드만 추가합니다.
	@PostMapping(value = "/save-with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResultData<Map<String, Object>> saveWithFile(@RequestPart("file") MultipartFile file,
			@RequestPart(value = "text", required = false) String text,
			@RequestPart(value = "groups", required = false) String groupsJson,
			@RequestPart(value = "visitId", required = false) Integer visitId,
			@RequestPart(value = "petId", required = false) Integer petId,
			@RequestPart(value = "docType", required = false) String docType) {
		if (file == null || file.isEmpty()) {
			return ResultData.from("F-OCR-SAVE", "업로드된 파일이 비어 있습니다.", "data", null);
		}

		java.util.List<java.util.Map<String, Object>> groups = null;
		boolean hasGroups = false;
		try {
			if (groupsJson != null && !groupsJson.isBlank()) {
				groups = objectMapper.readValue(groupsJson,
						new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {
						});
				hasGroups = (groups != null && !groups.isEmpty());
			}
		} catch (Exception ignore) {
		}

		boolean hasText = (text != null && !text.isBlank());
		if (!hasText && !hasGroups) {
			return ResultData.from("F-OCR-SAVE", "저장할 그룹/텍스트가 없습니다.", "data", null);
		}

		try {
			String fileUrl = saveFileAndReturnUrl(file.getBytes(), file.getOriginalFilename());

			// ✅ auto이고 groups가 있으면 실무상 대부분 lab → lab로 고정
			String effectiveDocType = (!"auto".equalsIgnoreCase(docType)) ? docType
					: (hasGroups ? "lab" : normalizeDocType("auto", text));

			OcrSaveVo vo = new OcrSaveVo();
			vo.setText(hasText ? text : null);
			vo.setGroups(groups); // ✅ 핵심
			vo.setVisitId(visitId);
			vo.setPetId(petId);
			vo.setDocType(effectiveDocType);
			vo.setFileUrl(fileUrl);

			return saveOcrText(vo);
		} catch (Exception e) {
			Map<String, Object> err = new java.util.HashMap<>();
			err.put("errorType", e.getClass().getSimpleName());
			err.put("error", e.getMessage());
			return ResultData.from("F-OCR-SAVE", "save-with-file 처리 중 오류", "data", err);
		}
	}

	private String normalizeDocType(String docType, String text) {
		// 사용자가 명시한 경우 그대로 사용
		if (docType != null && !docType.isBlank() && !"auto".equalsIgnoreCase(docType)) {
			return docType.trim().toLowerCase();
		}
		// [수정] 간단판 ocrFormatService.suggestDocType(...) 대신 정교한 분류기로 교체
		Map<String, Object> guess = suggestDocTypeWithConfidence(text);
		String suggested = (String) guess.get("type");
		return (suggested == null || suggested.isBlank()) ? "receipt" : suggested.toLowerCase();
	}

	@SuppressWarnings("unchecked")
	private String guessDocTypeFromGroups(java.util.List<java.util.Map<String, Object>> groups) {
		if (groups == null || groups.isEmpty())
			return "diagnosis";
		Object itemsObj = groups.get(0).get("items");
		if (itemsObj instanceof java.util.List<?> list && !list.isEmpty()) {
			Object first = list.get(0);
			if (first instanceof java.util.Map<?, ?> m) {
				// ✅ receipt 형태: type=line/summary 가 보이면 영수증
				Object t = m.get("type");
				if ("line".equals(t) || "summary".equals(t))
					return "receipt";

				// lab 형태: {name, value, ref_low...}
				if (m.containsKey("name") && (m.containsKey("value") || m.containsKey("ref_low")))
					return "lab";

				// diagnosis 형태: {key:"doctor"/"hospital"...}
				if (m.containsKey("key") && m.containsKey("value"))
					return "diagnosis";
			}
		}
		return "diagnosis";
	}

}
