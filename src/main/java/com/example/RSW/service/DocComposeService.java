// src/main/java/com/example/RSW/service/DocComposeService.java
package com.example.RSW.service;

import com.example.RSW.dto.DocEnvelopeDto;
import com.example.RSW.repository.MedicalDocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocComposeService {
    private final MedicalDocumentRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    public Map<String,Object> getStructuredByDocId(int docId) {
        DocEnvelopeDto e = repo.selectDocEnvelopeById(docId);
        if (e == null) throw new NoSuchElementException("document not found: " + docId);

        // --- document ---
        Map<String,Object> document = new LinkedHashMap<>();
        document.put("id", e.getDocumentId());
        document.put("visitId", e.getVisitId());
        document.put("docType", e.getDocType());
        document.put("fileUrl", e.getFileUrl());
        putIfNotNull(document, "provider", e.getHospital());
        if (e.getVisitDate() != null) {
            document.put("date", e.getVisitDate().toLocalDate().toString());
        }

        // --- pet ---
        Map<String,Object> pet = new LinkedHashMap<>();
        putIfNotNull(pet, "id",        e.getPetId2()!=null ? e.getPetId2() : e.getPetId());
        putIfNotNull(pet, "name",      e.getPetName());
        putIfNotNull(pet, "species",   e.getSpecies());
        putIfNotNull(pet, "sex",       e.getSex());
        putIfNotNull(pet, "birthDate", e.getBirthDate());

        // --- meta ---
        Map<String,Object> meta = new LinkedHashMap<>();
        putIfNotNull(meta, "notes", e.getNotes());

        // --- OCR JSON ---
        Map<String,Object> ocr = parseSafe(e.getOcrJson());

        // 핵심: groups[].items 평탄화해서 labs 뽑기
        List<Map<String,Object>> labs = extractLabs(ocr);

        // 문서 날짜가 비어 있으면 groups[].date로 보정
        if (!document.containsKey("date")) {
            String gdate = sniffGroupDate(ocr);
            if (gdate != null && !gdate.isBlank()) document.put("date", gdate);
        }

        // 처방은 아직 없으면 빈 배열
        Object rx = guessTopLevelList(ocr, "prescriptions","meds","rx");

        // --- 결과 합치기 ---
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("document", document);
        out.put("pet",      pet);
        out.put("labs",     labs);
        out.put("prescriptions", rx instanceof List ? rx : List.of());
        out.put("meta",     meta);
        return out;
    }

    /* ---------- explain ---------- */
    public Map<String, Object> explainMarkdownByDocId(int docId) {
        Map<String, Object> s = getStructuredByDocId(docId);

        @SuppressWarnings("unchecked")
        var document = (Map<String,Object>) s.getOrDefault("document", Map.of());
        @SuppressWarnings("unchecked")
        var pet      = (Map<String,Object>) s.getOrDefault("pet", Map.of());
        @SuppressWarnings("unchecked")
        var labs     = (List<Map<String,Object>>) s.getOrDefault("labs", List.of());

        StringBuilder md = new StringBuilder();
        md.append("# 보호자 설명\n\n");
        md.append("- 동물: ").append(nz(pet.get("name"), nz(pet.get("pet_name"), "-")))
                .append(" (").append(nz(pet.get("species"), "-")).append(")\n");
        md.append("- 병원: ").append(nz(document.get("provider"), "-"))
                .append(" / 방문일: ").append(nz(document.get("date"), "-")).append("\n\n");

        if (labs.isEmpty()) {
            md.append("이번 문서에서 분석 가능한 검사 항목이 없었습니다.\n");
        } else {
            md.append("## 검사 요약\n");
            int abn = 0;
            for (Map<String,Object> l : labs) {
                Double v  = toD(l.get("value"));
                Double lo = coalesceD(l.get("ref_low"), l.get("refLow"));
                Double hi = coalesceD(l.get("ref_high"), l.get("refHigh"));
                String name = String.valueOf(nz(l.get("name"), nz(l.get("code"), "?")));
                String unit = String.valueOf(nz(l.get("unit"), ""));

                String flag = "정상";
                if (v!=null && (lo!=null || hi!=null)) {
                    if (hi!=null && v>hi) flag="상승";
                    else if (lo!=null && v<lo) flag="저하";
                }
                if (!"정상".equals(flag)) abn++;

                md.append("- **").append(name).append("**: ")
                        .append(v!=null? trim(v):"-").append(unit.isBlank()? "":" "+unit);
                if (lo!=null || hi!=null) md.append(" (참고 ").append(trim(lo)).append(" ~ ").append(trim(hi)).append(")");
                md.append(" → ").append(flag).append("\n");
            }
            md.append("\n총 ").append(abn).append("개 항목이 참고범위를 벗어났습니다.\n");
        }

        return Map.of("markdown", md.toString());
    }

    /* ---------- OCR helpers ---------- */

    private Map<String,Object> parseSafe(String json){
        if (json==null || json.isBlank()) return Map.of();
        try { return om.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>(){}); }
        catch(Exception ignore){ return Map.of(); }
    }

    /** labs, lab_results, items 같은 '최상위' 배열만 조회 (보조용) */
    private Object guessTopLevelList(Map<String,Object> m, String... keys){
        for(String k: keys){
            Object v = m.get(k);
            if (v instanceof List) return v;
            if (v instanceof Map<?,?> map && map.get("items") instanceof List<?> lst) return lst;
        }
        return List.of();
    }

    /** groups[].items 평탄화 + refRange → ref_low/ref_high 보완 */
    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> extractLabs(Map<String,Object> ocr){
        List<Map<String,Object>> out = new ArrayList<>();
        if (ocr == null || ocr.isEmpty()) return out;

        // 1) 이미 최상위에 labs 배열이 있으면 그대로
        Object direct = ocr.get("labs");
        if (direct instanceof List<?> dl) {
            for (Object it : dl) if (it instanceof Map<?,?> m) {
                Map<String,Object> row = new LinkedHashMap<>((Map<String,Object>) m);
                if (!row.containsKey("ref_low") && !row.containsKey("ref_high")) splitRefRange(row);
                out.add(row);
            }
            return out;
        }

        // 2) groups[].items 구조
        Object g = ocr.get("groups");
        if (g instanceof List<?> groups) {
            for (Object go : groups) {
                if (!(go instanceof Map<?,?> gm)) continue;
                Object items = gm.get("items");
                if (!(items instanceof List<?> lst)) continue;
                for (Object io : lst) {
                    if (io instanceof Map<?,?> im) {
                        Map<String,Object> row = new LinkedHashMap<>((Map<String,Object>) im);
                        if (!row.containsKey("ref_low") && !row.containsKey("ref_high")) splitRefRange(row);
                        out.add(row);
                    }
                }
            }
        }

        // 3) 최상위 items 배열도 보조로 지원
        if (out.isEmpty()) {
            Object items = ocr.get("items");
            if (items instanceof List<?> lst) {
                for (Object io : lst) {
                    if (io instanceof Map<?,?> im) {
                        Map<String,Object> row = new LinkedHashMap<>((Map<String,Object>) im);
                        if (!row.containsKey("ref_low") && !row.containsKey("ref_high")) splitRefRange(row);
                        out.add(row);
                    }
                }
            }
        }
        return out;
    }

    /** refRange("a ~ b")를 ref_low/ref_high로 쪼개 넣기 */
    private void splitRefRange(Map<String,Object> row){
        Object rr = row.get("refRange");
        if (!(rr instanceof String s) || s.isBlank()) return;
        String cleaned = s.replace("—","-").replace("–","-");
        String[] parts = cleaned.split("~");
        if (parts.length == 2) {
            Double lo = toD(parts[0].trim().replaceAll("[^0-9+\\-\\.]", ""));
            Double hi = toD(parts[1].trim().replaceAll("[^0-9+\\-\\.]", ""));
            if (lo != null) row.put("ref_low",  lo);
            if (hi != null) row.put("ref_high", hi);
        }
    }

    /** groups[].date → yyyy-MM-dd */
    private String sniffGroupDate(Map<String,Object> ocr){
        Object g = ocr.get("groups");
        if (g instanceof List<?> groups) {
            for (Object go : groups) {
                if (go instanceof Map<?,?> gm) {
                    Object d = gm.get("date");
                    if (d instanceof String s && s.length() >= 10) return s.substring(0,10);
                }
            }
        }
        return null;
    }

    /* ---------- misc helpers ---------- */
    private static void putIfNotNull(Map<String,Object> m, String k, Object v){
        if (v != null) m.put(k, v);
    }
    private static Object nz(Object v, Object def){ return v==null?def:v; }
    private static Double toD(Object o){ try{ return o==null?null:Double.parseDouble(String.valueOf(o)); }catch(Exception e){ return null; } }
    private static Double coalesceD(Object a, Object b){ Double x=toD(a); return x!=null?x:toD(b); }
    private static String trim(Double d){ if (d==null) return "-"; String s=String.format(java.util.Locale.US,"%.4f",d); return s.replaceAll("0+$","").replaceAll("\\.$",""); }
}
