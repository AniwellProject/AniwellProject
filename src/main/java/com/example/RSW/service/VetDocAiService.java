package com.example.RSW.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VetDocAiService {

    @Value("${openai.api-base:https://api.openai.com/v1}")
    private String apiBase;

    @Value("${openai.api-key:}")
    private String apiKey;

    private final ObjectMapper om = new ObjectMapper();

    private WebClient client() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY가 설정되지 않았습니다.");
        }
        return WebClient.builder()
                .baseUrl(apiBase)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** ▶ JSON을 보호자 친화 설명(마크다운)으로 변환 */
    // 기존 메서드 유지 (호환)
    public String explainJson(Object payload) { return explainJson(payload, true); } // 기본을 상세모드로

    public String explainJson(Object payload, boolean detailed) {
        try {
            String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

            // ✅ "사전 없이" 자동으로 약어 식별·한글화·해설까지 하도록 강제
            String INSTRUCTIONS = """
      너는 반려동물 보호자에게 검사결과를 '처음 듣는 사람도 이해'할 수 있게 설명하는 한국어 안내자다.
      아래 JSON에서 텍스트/표/수치를 읽고, 등장하는 영문 약어를 자동으로 식별해서
      한국어 병칭과 쉬운 설명을 붙여라. (예: 알라닌 아미노전달효소(ALT) — 간세포 손상 지표)

      반드시 아래 **출력 형식**을 Markdown으로 지켜라.

      # 요약
      - 이상 소견(높음/낮음)만 굵게 2~4개로 정리. (예: **ALT 높음**, **총콜레스테롤 높음**)

      # 용어 사전(자동)
      | 약어 | 한국어 병칭 | 한 줄 설명 |
      |---|---|---|
      - 본문에 나온 약어만 표에 적어라.
      - 확실하지 않으면 '미확인'이라고 쓰고, 괄호에 '추정'으로 간단히 이유를 적어라.

      # 결과 표
      | 항목(한국어/영문) | 결과 | 정상범위 | 판정 | 한 줄 의미 |
      |---|---:|---|:---:|---|
      - 결과/단위/정상범위는 입력에서 보이는 값을 우선 사용.
      - 정상범위가 없으면 '일반 참고치(추정)'라고 표기하고 단정하지 말 것.
      - 판정은 '정상보다 높음/정상보다 낮음/정상' 중 하나.

      # 항목별 해설
      - 각 항목마다:
        - 무엇을 보는 검사인지 (한 문장)
        - 높음/낮음의 **흔한 원인** 2~4개 (개/고양이에서 흔한 예)
        - 다음 단계: 추가로 고려할 검사/영상/요검사 등
        - 집에서 관리 팁 1~2개 (사람약 금지 포함)

      # 위험 신호 & 즉시 내원 기준
      - 예: 황달(잇몸/눈 흰자 노랗다), 반복 구토/무기력, 검은색 변, 소변 급감 등

      # 다음 단계
      - 재검 권장 시점(경도 2–4주, 고도 1–3일 내 등)과 추천 추가 검사
      - 최종 판단은 주치의 상담에 따르라는 안내

      작성 규칙:
      - 전 구간에서 **한국어 병칭 + (영문 약어)** 형태로 표기.
      - 숫자/단위/정상범위는 원문 우선. 없으면 '자료 없음' 또는 '참고치(추정)'로 표기.
      - 과도한 단정 금지. 보호자 눈높이로 간단·친절·정확하게.
    """;

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "instructions", INSTRUCTIONS,
                    "input", List.of(
                            Map.of("role","user","content", List.of(
                                    Map.of("type","input_text",
                                            "text", "다음 JSON을 보호자용 설명으로 바꿔줘. 영문 약어는 자동으로 찾아 '용어 사전(자동)'에 정의하고, 전체 본문에서 한국어 병칭으로 설명해줘.\n\n" + json)
                            ))
                    )
            );

            Map<?,?> resp = callResponses(body);
            String text = extractText(resp);
            return (text == null || text.isBlank()) ? "설명 생성 실패: 빈 응답" : text;

        } catch (Exception e) {
            return "설명 생성 실패: " + String.valueOf(e.getMessage());
        }
    }


    // ---- 공통 HTTP ----
    @SuppressWarnings("unchecked")
    private Map<?,?> callResponses(Map<String, Object> body) {
        return client().post().uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        (ClientResponse r) -> r.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("OpenAI " + r.statusCode() + " -> " + b)))
                )
                .bodyToMono(Map.class)
                .block();
    }

    /** Responses API 출력 안전 파서 */
    @SuppressWarnings("unchecked")
    private String extractText(Map<?,?> resp) {
        // 1) output_text 헬퍼
        Object ot = resp.get("output_text");
        if (ot instanceof String s && !s.isBlank()) return s;

        // 2) output[] 경로 (Responses API)
        Object out = resp.get("output");
        if (out instanceof List<?> list && !list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                if (!(item instanceof Map<?,?> m1)) continue;
                Object content = m1.get("content");
                if (content instanceof List<?> parts) {
                    for (Object p : parts) {
                        if (!(p instanceof Map<?,?> m2)) continue;
                        String type = String.valueOf(m2.get("type"));
                        if (!"text".equals(type) && !"output_text".equals(type)) continue;

                        Object textNode = m2.get("text");
                        if (textNode instanceof Map<?,?> tm) {
                            Object v = tm.get("value");
                            if (v instanceof String s) sb.append(s);
                        } else if (textNode instanceof String s) {
                            sb.append(s);
                        }
                    }
                }
            }
            String s = sb.toString().trim();
            if (!s.isEmpty()) return s;
        }

        // 3) (호환) chat.completions 스타일
        Object choices = resp.get("choices");
        if (choices instanceof List<?> ch && !ch.isEmpty()) {
            Object first = ch.get(0);
            if (first instanceof Map<?,?> fm) {
                Object msg = fm.get("message");
                if (msg instanceof Map<?,?> mm) {
                    Object c = mm.get("content");
                    if (c instanceof String s) return s;
                }
            }
        }
        return null;
    }

    private static final ObjectMapper LENIENT_OM = new ObjectMapper();
    static {
        LENIENT_OM.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        LENIENT_OM.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        LENIENT_OM.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        LENIENT_OM.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> mapToTableJsonLoose(Object payload) {
        try {
            // 1) 원본 JSON 문자열화
            String raw = om.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

            // 2) 지시문: 스키마 강제 없이, "JSON만" 출력하도록 강하게 요구
            String INSTRUCTIONS = """
        너는 입력 JSON을 읽고, 아래 테이블/칼럼 정의에 맞춘 "테이블별 행 배열" JSON만 생성한다.

        테이블/칼럼:
        - medical_document: { pet_id(int), visit_id(int|null), doc_type('lab'|'prescription'|'memo'),
                              source(string, 기본 'ai'), visit_date('yyyy-MM-dd'|null) }
          • 각 행에 임시 식별자 _ref (예: "D1") 추가 (실제 PK 아님)
        - lab_result_detail: { document_ref(string), test_name(string), result_value(number|null),
                               unit(string|null), ref_low(number|null), ref_high(number|null),
                               flag('L'|'N'|'H'), notes(string|null) }
          • "123 U/L" → result_value=123, unit="U/L"; "10-100" → ref_low=10, ref_high=100
        - prescription_detail: { document_ref(string), drug_name(string), dose_value(number|null),
                                 dose_unit(string|null), frequency(string|null),
                                 duration_days(int|null), route(string|null), notes(string|null) }
          • "20 mg" → dose_value=20, dose_unit="mg"

        출력 형식(아래 3개 키만 사용):
        {
          "medical_document": [ ... ],
          "lab_result_detail": [ ... ],
          "prescription_detail": [ ... ]
        }

        규칙:
        - 오직 JSON만 출력. 코드블록/마크다운/설명/주석 금지.
        - 지정 키 외 임의 키 금지.
        - 값이 없으면 null 또는 필드 생략 가능. 최상위 3키는 항상 포함(빈 배열 허용).
        """;

            // 3) Responses API 호출 바디 (스키마/format 지정 안 함 → 스키마 오류 회피)
            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "instructions", INSTRUCTIONS,
                    "input", java.util.List.of(
                            Map.of("role", "user", "content", java.util.List.of(
                                    Map.of("type", "input_text",
                                            "text", "아래 원본 JSON을 테이블별 행으로 매핑해줘:\n\n" + raw)
                            ))
                    )
            );

            Map<?,?> resp = callResponses(body);
            String out = extractText(resp);
            if (out == null || out.isBlank()) throw new RuntimeException("빈 응답");

            // 4) 1차: 바로 파싱 시도
            Map<String, Object> parsed;
            try {
                parsed = om.readValue(out, Map.class);
            } catch (Exception ignore) {
                // 5) 2차: ```json ... ``` 제거 + 가장 큰 { ... } 블록만 뽑아서 관대 파싱
                String cleaned = stripCodeFences(out);
                String objectOnly = largestJsonObject(cleaned);
                parsed = LENIENT_OM.readValue(objectOnly, Map.class);
            }

            // 6) 안정화: 최상위 3키만 유지 + 배열 형태 보정
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("medical_document", asArray(parsed.get("medical_document")));
            result.put("lab_result_detail", asArray(parsed.get("lab_result_detail")));
            result.put("prescription_detail", asArray(parsed.get("prescription_detail")));
            return result;

        } catch (Exception e) {
            throw new RuntimeException("매핑(스키마없음) 실패: " + e.getMessage(), e);
        }
    }

    // 없는/단일 객체 값을 배열로 보정
    @SuppressWarnings("unchecked")
    private java.util.List<Object> asArray(Object v) {
        if (v == null) return java.util.List.of();
        if (v instanceof java.util.List) return (java.util.List<Object>) v;
        return java.util.List.of(v);
    }

    // ```...``` 또는 ```json ...``` 코드펜스 제거
    private String stripCodeFences(String s) {
        return s.replaceAll("(?s)```(?:json)?\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
    }

    // 문자열에서 가장 큰 JSON 객체 { ... } 블록 하나만 추출
    private String largestJsonObject(String s) {
        int bestStart = -1, bestEnd = -1, depth = 0, start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    if (bestStart == -1 || (i - start) > (bestEnd - bestStart)) {
                        bestStart = start; bestEnd = i;
                    }
                    start = -1;
                }
            }
        }
        if (bestStart >= 0 && bestEnd > bestStart) return s.substring(bestStart, bestEnd + 1);
        return s; // 못 찾으면 원문 반환(파서가 에러 처리)
    }

    public String explain(com.example.RSW.dto.ExplainRequest req, boolean detailed) {
        return explainJson(req, detailed); // 이미 있는 메서드를 재사용
    }
    public String explain(com.example.RSW.dto.ExplainRequest req) {
        return explain(req, true);
    }

}
