package com.example.RSW.controller;

import com.example.RSW.dto.LitterAnalysisDto;
import com.example.RSW.dto.LitterAnalyzeResponse;
import com.example.RSW.repository.LitterEventRepository;
import com.example.RSW.service.IngestService;
import com.example.RSW.service.PythonRunner;
import com.example.RSW.vo.LitterEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/litter")
public class IngestController {

    private final IngestService ingestService;
    private final PythonRunner pythonRunner;
    private final LitterEventRepository litterEventMapper;
    private final ObjectMapper om;

    @Value("${aniwell.device.key:}")
    private String deviceKey;

    @Value("${aniwell.ingest.cleanup:true}")
    private boolean cleanupEnabled;

    private void assertDevice(String header) {
        if (StringUtils.hasText(deviceKey) && !deviceKey.equals(header)) {
            // 401로 명확히 반환 (500 방지)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized device");
        }
    }

    /** 프레임 1장 업로드: Content-Type: image/jpeg, raw body */
    @PostMapping(
            value = "/ingest/frame",
            consumes = { MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE },
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> ingestFrame(
            @RequestParam String session,
            @RequestParam int seq,
            @RequestHeader(value = "X-Device-Key", required = false) String key,
            @RequestBody byte[] jpeg
    ) throws Exception {
        assertDevice(key);
        if (jpeg == null || jpeg.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");
        }
        if (seq < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seq must be >= 0");
        }
        ingestService.saveJpegFrame(session, seq, jpeg);
        return ResponseEntity.ok("OK");
    }

    /** 세션 완료: 프레임을 mp4로 조립 → 분석 → (조건부) DB 저장 → 응답 */
    @PostMapping(value = "/ingest/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LitterAnalyzeResponse<LitterAnalysisDto>> complete(
            @RequestParam String session,
            @RequestParam Long petId,
            @RequestParam(defaultValue = "8") int fps,
            @RequestParam(value = "linger", required = false) Double linger,
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            @RequestParam(value = "frames", required = false) Integer frames,
            @RequestParam(value = "squatOnly", defaultValue = "false") boolean squatOnly,
            @RequestHeader(value = "X-Device-Key", required = false) String key
    ) throws Exception {
        assertDevice(key);

        Path mp4 = ingestService.assembleMp4(session, fps);
        if (mp4 == null || !mp4.toFile().exists() || mp4.toFile().length() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no frames for session or mp4 empty");
        }

        // Python 분석 호출 (squatOnly/frames/force 반영)
        LitterAnalysisDto dto = pythonRunner.runOnVideo(
                mp4.toFile(), linger, force, frames, squatOnly
        );

        // 무시 케이스는 저장 스킵(정책에 따라 바꿀 수 있음)
        if (!Boolean.TRUE.equals(dto.getIgnored())) {
            LitterEvent e = new LitterEvent();
            e.setPetId(petId);
            e.setDetectedAt(LocalDateTime.now());
            e.setType(dto.getType());
            e.setConfidence(dto.getConfidence());
            e.setVisualSignalsJson(om.writeValueAsString(dto.getVisual_signals()));
            e.setAnomaliesJson(om.writeValueAsString(dto.getAnomalies()));
            e.setNotes(dto.getNotes());
            e.setSourceVideo(mp4.getFileName().toString());
            e.setLogId(null);
            litterEventMapper.insert(e);
        }

        if (cleanupEnabled) {
            try { ingestService.cleanup(session); } catch (Exception ignored) {}
        }

        if (Boolean.TRUE.equals(dto.getIgnored())) {
            return ResponseEntity.ok(new LitterAnalyzeResponse<>("IGNORED", dto.getIgnoreReason(), dto));
        }
        return ResponseEntity.ok(new LitterAnalyzeResponse<>("OK", "분석완료", dto));
    }
}
