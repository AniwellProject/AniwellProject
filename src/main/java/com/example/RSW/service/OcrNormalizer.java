package com.example.RSW.service;

import com.example.RSW.dto.ExplainRequest;
import com.example.RSW.vo.MedicalDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * medical_document.ocr_json → 표준 ExplainRequest 정규화
 * - 이제부터는 JSON(groups) 우선
 * - groups가 없거나 비면 텍스트 폴백 파서
 * - 오입력된 doc_type이라도 내용이 검사형이면 lab로 전환
 */
@Service
public class OcrNormalizer {

    private final ObjectMapper om = new ObjectMapper();

    /* ==================== 엔트리 ==================== */

    public ExplainRequest fromDocument(MedicalDocument doc) {
        var document = new ExplainRequest.Document(providerFrom(doc), dateFrom(doc));
        var labs = new ArrayList<ExplainRequest.Lab>();
        var rxs  = new ArrayList<ExplainRequest.Prescription>();
        String notes;

        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : om.createObjectNode();

            // 타입 결정: ocr_json.docType → 문서 docType → 텍스트 휴리스틱
            String type = safe(text(root.get("docType")));
            if (type.isBlank()) type = safe(doc.getDocType());
            String rawText = text(root.get("text"));
            if (!"lab".equals(type) && looksLikeLabText(rawText)) type = "lab";

            // ===== 1) groups 우선 처리 =====
            JsonNode groups = root.get("groups");
            boolean handledByGroups = false;

            if (groups != null && groups.isArray() && groups.size() > 0) {
                // 그룹 중 아이템이 있는 최신 그룹 선별
                JsonNode best = pickBestGroup(groups);
                if (best != null) {
                    String grpDate = text(best.get("date"));
                    JsonNode items = best.get("items");
                    if (items != null && items.isArray()) {
                        if ("lab".equals(type) || looksLikeGroupIsLab(items)) {
                            labs.addAll(toLabsFromGroupItems(items));
                            handledByGroups = true;
                            notes = "OCR groups 기준 파싱됨 · date=" + (grpDate==null? "-" : grpDate)
                                    + " · items=" + items.size();
                            return assemble(document, labs, rxs, notes);
                        }
                        // TODO: prescription/receipt 형태로 groups를 저장하는 스펙이 생기면 여기 확장
                    }
                }
            }

            // ===== 2) groups로 처리 못했으면 과거 로직 유지 =====
            switch (type) {
                case "lab" -> {
                    if (!handledByGroups) {
                        // 과거 포맷(labs 배열) 지원
                        if (root.has("labs") && root.get("labs").isArray()) {
                            for (JsonNode l : root.get("labs")) {
                                labs.add(new ExplainRequest.Lab(
                                        text(l.get("code")), text(l.get("name")),
                                        dbl(l.get("value")), text(l.get("unit")),
                                        dbl(l.get("ref_low")), dbl(l.get("ref_high"))
                                ));
                            }
                            notes = "이전 labs 배열 포맷에서 파싱됨";
                        } else if (!rawText.isBlank()) {
                            labs.addAll(parseLabText(rawText));
                            notes = "텍스트 폴백 파싱됨";
                        } else {
                            notes = "내용이 없습니다.";
                        }
                    } else {
                        notes = "OCR groups 기준 파싱됨";
                    }
                }
                case "prescription" -> {
                    if (root.has("prescriptions") && root.get("prescriptions").isArray()) {
                        for (JsonNode p : root.get("prescriptions")) {
                            rxs.add(new ExplainRequest.Prescription(
                                    text(p.get("drug")),
                                    dbl(p.get("dose")),
                                    text(p.get("dose_unit")),
                                    text(p.get("freq")),
                                    text(p.get("route")),
                                    intOrNull(p.get("days"))
                            ));
                        }
                        notes = "처방전 JSON에서 파싱됨";
                    } else if (root.has("drugs") && root.get("drugs").isArray()) {
                        for (JsonNode n : root.get("drugs")) {
                            String drug = text(n);
                            if (!drug.isBlank())
                                rxs.add(new ExplainRequest.Prescription(drug, null, null, null, null, null));
                        }
                        notes = "단순 약품명 배열에서 파싱됨";
                    } else {
                        notes = (rawText.isBlank() ? "내용이 없습니다." : "처방 JSON이 없어 텍스트 파서는 생략");
                    }
                }
                case "receipt" -> {
                    if (!rawText.isBlank() && looksLikeLabText(rawText)) {
                        labs.addAll(parseLabText(rawText));
                        notes = "영수증으로 저장되었으나 검사 텍스트로 자동 인식됨";
                        break;
                    }
                    String items = joinComma(root.get("items"));
                    String total = text(root.get("total"));
                    String ts    = text(root.at("/meta/ts"));
                    notes = buildReceiptNotes(items, total, ts);
                }
                default -> {
                    notes = (root.isMissingNode() || root.size()==0) ? null : ("원본 JSON: " + root.toString());
                }
            }

            return assemble(document, labs, rxs, notes);

        } catch (Exception e) {
            notes = "정규화 오류: " + e.getMessage();
            return assemble(document, new ArrayList<>(), new ArrayList<>(), notes);
        }
    }

    private ExplainRequest assemble(ExplainRequest.Document document,
                                    List<ExplainRequest.Lab> labs,
                                    List<ExplainRequest.Prescription> rxs,
                                    String notes) {
        return new ExplainRequest(
                document,
                new ExplainRequest.Pet(null,null,null,null,null,null), // 펫은 어셈블러에서 주입
                labs, rxs, new ExplainRequest.Meta(notes)
        );
    }

    /* ==================== groups → labs ==================== */

    /** groups 배열 중에서 가장 최근(파싱 성공한 최대 날짜) & items 있는 그룹을 선택 */
    private JsonNode pickBestGroup(JsonNode groups) {
        JsonNode best = null;
        LocalDateTime bestDt = null;

        for (JsonNode g : groups) {
            JsonNode items = g.get("items");
            if (items == null || !items.isArray() || items.size()==0) continue;

            String d = text(g.get("date"));
            LocalDateTime dt = parseDateFlexible(d);
            if (dt == null) {
                // 날짜가 없으면, 일단 후보로만 (날짜가 있는 그룹을 우선)
                if (best == null) best = g;
                continue;
            }
            if (bestDt == null || dt.isAfter(bestDt)) {
                bestDt = dt;
                best = g;
            }
        }
        // 모두 날짜 파싱 실패이거나 전부 비었으면 null
        return best;
    }

    private LocalDateTime parseDateFlexible(String s) {
        if (s == null || s.isBlank()) return null;
        List<DateTimeFormatter> fmts = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ISO_DATE_TIME,         // 2024-04-08T16:34:08[.SSS]
                DateTimeFormatter.ofPattern("yyyy-MM-dd")// 2024-04-08
        );
        for (DateTimeFormatter f : fmts) {
            try {
                // 날짜만 있으면 00:00:00으로 보정
                if (f == fmts.get(2)) {
                    return LocalDate.parse(s, f).atStartOfDay();
                }
                return LocalDateTime.parse(s, f);
            } catch (DateTimeParseException ignore) {}
        }
        return null;
    }

    /** 해당 items가 검사 항목 형태인지 간단 점검 */
    private boolean looksLikeGroupIsLab(JsonNode items) {
        if (items == null || !items.isArray()) return false;
        int score = 0;
        for (JsonNode it : items) {
            if (it.hasNonNull("name")) score++;
            if (it.has("value")) score++;
            if (it.has("unit")) score++;
            if (it.has("ref_low") || it.has("ref_high") || it.has("refRange")) score++;
            if (score >= 3) return true;
        }
        return false;
    }

    /** 단일 그룹의 items → labs (동일 name은 '마지막 값'이 우선) */
    private List<ExplainRequest.Lab> toLabsFromGroupItems(JsonNode items) {
        LinkedHashMap<String, ExplainRequest.Lab> map = new LinkedHashMap<>();
        for (JsonNode it : items) {
            String name = text(it.get("name"));
            if (name.isBlank()) continue;

            Double val  = dbl(it.get("value"));
            String unit = text(it.get("unit"));

            // ref_low/ref_high 없으면 refRange 문자열에서 추출 시도
            Double lo = dbl(it.get("ref_low"));
            Double hi = dbl(it.get("ref_high"));
            if ((lo == null || hi == null) && it.hasNonNull("refRange")) {
                double[] lr = parseRefRangeString(text(it.get("refRange")));
                if (lo == null) lo = (lr[0] != Double.NaN ? lr[0] : null);
                if (hi == null) hi = (lr[1] != Double.NaN ? lr[1] : null);
            }

            ExplainRequest.Lab row = new ExplainRequest.Lab(name, name, val, unit, lo, hi);
            // 동일 항목명은 '마지막 등장' 값으로 교체
            map.put(name, row);
        }
        return new ArrayList<>(map.values());
    }

    /** "a ~ b" 혹은 "a-b" 에서 숫자 2개를 뽑아낸다. 실패 시 NaN */
    private double[] parseRefRangeString(String s) {
        if (s == null) return new double[]{Double.NaN, Double.NaN};
        Matcher m = Pattern.compile("([-+]?\\d+(?:\\.\\d+)?)\\s*[~\\-–]\\s*([-+]?\\d+(?:\\.\\d+)?)").matcher(s);
        if (m.find()) {
            try {
                return new double[]{ Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)) };
            } catch (Exception ignore) {}
        }
        return new double[]{Double.NaN, Double.NaN};
    }

    /* ==================== 텍스트 감지 ==================== */

    private boolean looksLikeLabText(String t) {
        if (t == null) return false;
        String s = t.toLowerCase(Locale.ROOT);
        if (s.contains("정상범위") || s.contains("reference")) return true;
        if (s.contains("u/l") || s.contains("mg/dl") || s.contains("g/dl") ||
                s.contains("pmol/l") || s.contains("μmol/l") || s.contains("mmhg") ||
                s.contains("mmol/l") || s.contains("(%)")) return true;
        return s.matches("(?s).*\\b(alt|ast|bun|crea|creatinine|ggt|bilirubin|glucose|cholesterol|albumin|amylase|lipase|sdma|fpl|ammonia|alkp|hct|hgb|po2|pco2|tco2|ph)\\b.*");
    }

    /* ==================== 텍스트 폴백 파서 (기존 개선 버전 유지) ==================== */

    /**
     * 상태 기반 라인 파서
     * - name → (optional: 숫자, ~, 단위, 범위) → 값(▲/▼/=) ...
     * - 여러 값이 나오면 "마지막 값" 채택(최근 측정치 가정)
     */
    private List<ExplainRequest.Lab> parseLabText(String body) {
        var out = new ArrayList<ExplainRequest.Lab>();

        String curName = null;
        String unit = null;
        Double refLo = null, refHi = null;
        Double pendingLo = null;

        Pattern NAME = Pattern.compile("^[A-Za-z가-힣\\[\\]()+/\\-_.\\s]+$");
        Pattern RANGE_TILDE_UNIT = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*~\\s*(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?");
        Pattern RANGE_HYPHEN_UNIT = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*-\\s*(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?");
        Pattern RANGE_SPACE_UNIT  = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s+(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?");
        Pattern UNIT_ONLY = Pattern.compile("^\\(([^)]+)\\)$");
        Pattern NUM_WITH_UNIT = Pattern.compile("^(-?\\d+(?:\\.\\d+)?)\\s*\\(([^)]+)\\)\\s*(?:\\(([▲▼=AV])\\))?$");
        Pattern JUST_NUM = Pattern.compile("^\\s*[-+]?\\d+(?:\\.\\d+)?\\s*$");

        for (String rawLine : body.split("\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            if (line.matches("(?i)^(검사명|정상범위|chemistry|hematology|storage:|urinalysis|hormone analysis).*$")) continue;
            if (line.equals("~") || line.equals("~ 0")) continue;

            // 1) 새 항목 이름
            if (!line.contains("~") && !line.matches(".*\\d.*") && NAME.matcher(line).matches()) {
                curName = line.replaceAll("[:\\-–]+$","").trim();
                unit = null; refLo = refHi = null; pendingLo = null;
                continue;
            }
            if (curName == null) continue;

            // 2) 범위
            Matcher m;
            m = RANGE_TILDE_UNIT.matcher(line);
            if (m.find()) {
                refLo = tryD(m.group(1)); refHi = tryD(m.group(2));
                if (m.group(3)!=null) unit = m.group(3).trim();
                pendingLo = null;
                continue;
            }
            m = RANGE_HYPHEN_UNIT.matcher(line);
            if (m.find()) {
                refLo = tryD(m.group(1)); refHi = tryD(m.group(2));
                if (m.group(3)!=null) unit = m.group(3).trim();
                pendingLo = null;
                continue;
            }
            m = RANGE_SPACE_UNIT.matcher(line);
            if (m.find() && line.split("\\s+").length<=4) {
                refLo = tryD(m.group(1)); refHi = tryD(m.group(2));
                if (m.group(3)!=null) unit = m.group(3).trim();
                pendingLo = null;
                continue;
            }

            // 3) 단위만
            Matcher um = UNIT_ONLY.matcher(line);
            if (um.find()) {
                unit = um.group(1).trim();
                continue;
            }

            // 4-a) 숫자만 → lo 후보
            if (JUST_NUM.matcher(line).matches() && refLo == null && refHi == null) {
                pendingLo = tryD(line);
                continue;
            }

            // 4-b) "숫자 (단위)" → 값 or 분할범위 결합
            Matcher nu = NUM_WITH_UNIT.matcher(line);
            if (nu.find()) {
                Double num = tryD(nu.group(1));
                String u = nu.group(2).trim();
                if (pendingLo != null && refLo == null && refHi == null) {
                    refLo = pendingLo; refHi = num; unit = u; pendingLo = null;
                    continue;
                }
                unit = (unit==null ? u : unit);
                addOrReplace(out, curName, num, unit, refLo, refHi);
                continue;
            }

            // 5) 값 라인 (*2+(200), !(500), 1.02(▼), 262(A) 등)
            Value v = extractValue(line);
            if (v != null) {
                addOrReplace(out, curName, v.num, unit, refLo, refHi);
                pendingLo = null;
            }
        }

        return out;
    }

    /** 결과 목록에서 동일 항목 이름이 이미 있으면 "마지막 값"으로 교체 */
    private void addOrReplace(List<ExplainRequest.Lab> out, String name, Double val, String unit,
                              Double refLo, Double refHi) {
        if (val == null && unit == null && refLo == null && refHi == null) return;
        int idx = -1;
        for (int i=out.size()-1;i>=0;i--) {
            if (Objects.equals(out.get(i).getName(), name)) { idx = i; break; }
        }
        var row = new ExplainRequest.Lab(name, name, val, unit, refLo, refHi);
        if (idx >= 0) out.set(idx, row); else out.add(row);
    }

    /** *2+(200), !(500), 1.02(▼), 262(A), -9.3(▼) 등에서 숫자/플래그 추출 */
    private Value extractValue(String line) {
        Matcher parenNum = Pattern.compile(".*\\(([-+]?\\d+(?:\\.\\d+)?)\\)\\s*(?:\\(([▲▼=AV])\\))?$").matcher(line);
        if (parenNum.matches()) {
            Double n = tryD(parenNum.group(1));
            return new Value(n, normalizeFlag(parenNum.group(2)));
        }
        Matcher numFlag = Pattern.compile("^\\s*([-+]?\\d+(?:\\.\\d+)?)\\s*\\(([▲▼=AV])\\)\\s*$").matcher(line);
        if (numFlag.matches()) {
            return new Value(tryD(numFlag.group(1)), normalizeFlag(numFlag.group(2)));
        }
        Matcher justNum = Pattern.compile("^\\s*([-+]?\\d+(?:\\.\\d+)?)\\s*$").matcher(line);
        if (justNum.matches()) {
            return new Value(tryD(justNum.group(1)), null);
        }
        return null;
    }

    private static String normalizeFlag(String s){
        if (s == null) return null;
        s = s.trim();
        if ("A".equalsIgnoreCase(s)) return "▲";
        if ("V".equalsIgnoreCase(s)) return "▼";
        return s;
    }

    private record Value(Double num, String flag) {}

    private static Double tryD(String s){
        try { return s==null? null : Double.valueOf(s); } catch(Exception e){ return null; }
    }

    /* ==================== 메타/공용 ==================== */

    private String providerFrom(MedicalDocument doc) {
        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : null;
            if (root != null) {
                String p = text(root.at("/meta/provider"));
                if (!p.isBlank()) return p;
            }
        } catch (Exception ignore) {}
        return "Aniwell";
    }

    private String dateFrom(MedicalDocument doc) {
        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : null;
            if (root != null) {
                String d = text(root.at("/meta/date"));
                if (!d.isBlank()) return d;
                String ts = text(root.at("/meta/ts"));
                if (!ts.isBlank()) return ts.length()>=10 ? ts.substring(0,10) : ts;
            }
        } catch (Exception ignore) {}
        return (doc.getCreatedAt()!=null) ? doc.getCreatedAt().toLocalDate().toString() : LocalDate.now().toString();
    }

    private static String joinComma(JsonNode arr){
        if (arr != null && arr.isArray()) {
            var list = new ArrayList<String>();
            arr.forEach(n -> list.add(text(n)));
            return String.join(", ", list);
        }
        return null;
    }

    private static String buildReceiptNotes(String items, String total, String ts){
        StringBuilder sb = new StringBuilder("영수증");
        if (ts != null && !ts.isBlank()) sb.append(" (").append(ts).append(")");
        sb.append(" · ");
        if (items != null && !items.isBlank()) sb.append("항목: ").append(items).append(" · ");
        sb.append("총액: ").append(total != null && !total.isBlank() ? total : "-");
        return sb.toString();
    }

    private static String safe(String s){ return s==null ? "" : s.toLowerCase(Locale.ROOT); }
    private static String text(JsonNode n){ return (n==null || n.isNull()) ? "" : n.asText(""); }
    private static Double dbl(JsonNode n){ return (n==null || n.isNull() || n.asText().isBlank()) ? null : n.asDouble(); }
    private static Integer intOrNull(JsonNode n){ return (n==null || n.isNull() || n.asText().isBlank()) ? null : n.asInt(); }
}
