package com.example.RSW.controller;

import com.example.RSW.service.*;
import com.example.RSW.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 공개 조회용 API (S3/CloudFront에서 호출)
 * - GET /api/pet/report?petId=1
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin( // 필요한 오리진만 정확히 허용
        origins = {
                "https://aniwell.s3.ap-northeast-2.amazonaws.com", // S3 객체 URL
                // "https://dxxxxx.cloudfront.net",                 // CloudFront 쓰면 추가
                "https://api.aniwell.kr"                            // 자체 호출(프록시) 시
        },
        allowCredentials = "false" // 세션/쿠키 안 쓰는 공개 조회면 false가 단순/안전
)
public class PetReportApiController {

    private final PetService petService;
    private final VisitService visitService;
    private final PetHealthService petHealthService;
    private final PrescriptionDetailService prescriptionDetailService;
    private final LabResultDetailService labResultDetailService;
    private final MedicalDocumentService medicalDocumentService;

    /**
     * 펫 리포트 (방문/처방/검사/문서 포함)
     * 응답 예:
     * {
     *   "pet": {...},
     *   "visits": [ { id, visitDate, hospital, ..., prescriptions:[], labResults:[], documents:[] }, ... ],
     *   "logs": [ ... ]
     * }
     */
    @GetMapping("/pet/report")
    public Map<String, Object> getReport(@RequestParam int petId) {
        Pet pet = petService.getPetsById(petId);
        List<Visit> visits = visitService.selectVisitsByPetId(petId);
        List<PetHealthLog> logs = petHealthService.getLogsByPetId(petId);

        // 방문별 상세 합치기
        List<Map<String, Object>> visitBlocks = new ArrayList<>();
        for (Visit v : (visits == null ? List.<Visit>of() : visits)) {
            int vid = v.getId();

            List<PrescriptionDetail> pres = prescriptionDetailService.selectByVisitId(vid);
            List<LabResultDetail> labs     = labResultDetailService.selectByVisitId(vid);
            List<MedicalDocument> docs     = medicalDocumentService.selectByVisitId(vid);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          v.getId());
            m.put("visitDate",   v.getVisitDate());  // LocalDateTime -> ISO로 직렬화
            m.put("hospital",    v.getHospital());
            m.put("doctor",      v.getDoctor());
            m.put("diagnosis",   v.getDiagnosis());
            m.put("notes",       v.getNotes());
            m.put("totalCost",   v.getTotalCost());
            m.put("prescriptions", pres != null ? pres : List.of());
            m.put("labResults",    labs != null ? labs : List.of());
            m.put("documents",     docs != null ? docs : List.of());
            visitBlocks.add(m);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pet",    pet);
        body.put("visits", visitBlocks);
        body.put("logs",   logs != null ? logs : List.of());
        return body;
    }

    /** 간단 헬스체크 */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true, "ts", System.currentTimeMillis());
    }
}
