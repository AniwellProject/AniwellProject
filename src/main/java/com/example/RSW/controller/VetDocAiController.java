package com.example.RSW.controller;

import com.example.RSW.dto.ExplainRequest;
import com.example.RSW.service.VetDocAiService;
import com.example.RSW.service.VetDocAssembler; // ⬅️ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class VetDocAiController {

    private final VetDocAiService ai;
    private final VetDocAssembler assembler; // ⬅️ 추가

    /** ▶ 아무 JSON이나 넣으면 보호자 설명(Markdown) 반환 */
    @PostMapping("/explain")
    public Map<String, Object> explain(@RequestBody Object json,
                                       @RequestParam(defaultValue = "false") boolean detailed) {
        String md = ai.explainJson(json, detailed);
        return Map.of("markdown", md);
    }

    /** ▶ medical_document.id 로 합쳐진 표준 JSON 반환 (좌측 입력창 채우기) */
    @GetMapping("/doc/{id}/structured") // ⬅️ 추가
    public ExplainRequest getStructured(@PathVariable int id) {
        return assembler.fromDocumentId(id); // 문서 JSON + 펫 정보 합쳤음
    }

    /** ▶ medical_document.id 로 보호자 설명(Markdown) 반환 (우측 미리보기/Report) */
    @GetMapping("/doc/{id}/explain") // ⬅️ 추가
    public Map<String, Object> explainFromDoc(@PathVariable int id,
                                              @RequestParam(defaultValue = "false") boolean detailed) {
        ExplainRequest req = assembler.fromDocumentId(id);
        String md = ai.explain(req, detailed); // VetDocAiService 오버로드 사용
        return Map.of("markdown", md);
    }

    /** 헬스체크용 */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true);
    }

    @PostMapping(
            value = "map",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> map(@RequestParam(required = false) Long petId,
                                   @RequestBody(required = false) Map<String, Object> payload) {

        if (petId != null && (payload == null || !payload.containsKey("petId"))) {
            payload = payload == null ? new HashMap<>() : new HashMap<>(payload);
            payload.put("petId", petId);
        }
        return ai.mapToTableJsonLoose(payload);
    }
}
