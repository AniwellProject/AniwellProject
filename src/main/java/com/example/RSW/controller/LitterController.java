package com.example.RSW.controller;

import com.example.RSW.dto.LitterAnalysisDto;
import com.example.RSW.dto.LitterAnalyzeResponse;
import com.example.RSW.repository.LitterEventRepository;
import com.example.RSW.service.PythonRunner;
import com.example.RSW.vo.LitterEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/litter")
public class LitterController {

    private final PythonRunner pythonRunner;
    private final LitterEventRepository litterEventRepo;
    private final ObjectMapper objectMapper;

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LitterAnalyzeResponse<LitterAnalysisDto>> analyze(
            @RequestPart("video") MultipartFile video,
            @RequestParam("petId") Long petId,                                 // ✅ 필수
            @RequestParam(value = "logId", required = false) Long logId,       // 선택
            @RequestParam(value = "sourceVideo", required = false) String src, // 선택(파일명/URL)
            @RequestParam(value = "linger", required = false) Double lingerSec,
            @RequestParam(value = "force",   required = false, defaultValue = "false") boolean force,
            @RequestParam(value = "frames",  required = false) Integer frames   // 선택: 기본값 덮어쓰기
    ) throws Exception {

        File temp = File.createTempFile("aniwell_", "_" + video.getOriginalFilename());
        FileCopyUtils.copy(video.getBytes(), temp);

        try {
            LitterAnalysisDto dto = pythonRunner.runOnVideo(temp, lingerSec, force, frames);

            // 무시 이벤트는 DB에 안 남기는 정책(원하면 아래 if 제거)
            if (!Boolean.TRUE.equals(dto.getIgnored())) {
                LitterEvent e = new LitterEvent();
                e.setPetId(petId);
                e.setDetectedAt(LocalDateTime.now()); // 필요하면 logId의 logDate로 대체
                e.setType(dto.getType());
                e.setConfidence(dto.getConfidence());
                e.setVisualSignalsJson(objectMapper.writeValueAsString(dto.getVisual_signals()));
                e.setAnomaliesJson(objectMapper.writeValueAsString(dto.getAnomalies()));
                e.setNotes(dto.getNotes());
                e.setSourceVideo(src != null ? src : video.getOriginalFilename());
                e.setLogId(logId);
                litterEventRepo.insert(e);
            }

            if (Boolean.TRUE.equals(dto.getIgnored())) {
                return ResponseEntity.ok(new LitterAnalyzeResponse<>("IGNORED", dto.getIgnoreReason(), dto));
            }
            return ResponseEntity.ok(new LitterAnalyzeResponse<>("OK", "분석완료", dto));

        } finally {
            if (temp.exists()) temp.delete();
        }
    }

    /** 최근 N개 조회 (UI/디버깅용) */
    @GetMapping(value="/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LitterEvent> recent(
            @RequestParam("petId") Long petId,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        return litterEventRepo.findRecentByPet(petId, Math.max(1, Math.min(limit, 200)));
    }

    // 헬스체크
    @GetMapping("/ping")
    public String ping() { return "pong"; }

    // === 하루 이벤트 목록 ===
    @GetMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listEvents(
            @RequestParam Long petId,
            @RequestParam String date // "yyyy-MM-dd"
    ) {
        var d = java.time.LocalDate.parse(date);
        var from = d.atStartOfDay();
        var to   = from.plusDays(1);

        var list = litterEventRepo.findByPetAndDate(petId, from, to);
        // 그대로 반환하면 ISO 포맷의 detectedAt 포함해서 JSON으로 떨어짐
        return ResponseEntity.ok(list);
    }

    // === 하루 요약(pee/poop/unknown) ===
    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> summary(
            @RequestParam Long petId,
            @RequestParam String date // "yyyy-MM-dd"
    ) {
        var d = java.time.LocalDate.parse(date);
        var from = d.atStartOfDay();
        var to   = from.plusDays(1);

        var rows = litterEventRepo.countByPetAndDateGroupByType(petId, from, to);
        int pee=0, poop=0, unknown=0;
        for (var r : rows) {
            String t = String.valueOf(r.get("type"));
            int c = ((Number) r.get("cnt")).intValue();
            switch (t) {
                case "pee" -> pee = c;
                case "poop" -> poop = c;
                default -> unknown = c;
            }
        }
        var body = new java.util.LinkedHashMap<String,Object>();
        body.put("date", date);
        body.put("pee", pee);
        body.put("poop", poop);
        body.put("unknown", unknown);
        body.put("total", pee + poop + unknown);
        return ResponseEntity.ok(body);
    }



    @GetMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE, params = {"petId","days"})
    public ResponseEntity<?> listEventsByDays(
            @RequestParam Long petId,
            @RequestParam int days,
            @RequestParam(defaultValue = "0")  int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        var to   = java.time.LocalDateTime.now();
        var from = to.minusDays(Math.max(1, days));

        // 레포지토리에 페이징이 없으면 일단 기간으로 긁고 메모리에서 슬라이싱
        var all  = litterEventRepo.findByPetAndDate(petId, from, to);
        all.sort((a,b)-> b.getDetectedAt().compareTo(a.getDetectedAt()));
        int start = Math.min(offset, all.size());
        int end   = Math.min(start + Math.max(1, limit), all.size());
        var page  = all.subList(start, end);

        return ResponseEntity.ok(page);
    }
}
