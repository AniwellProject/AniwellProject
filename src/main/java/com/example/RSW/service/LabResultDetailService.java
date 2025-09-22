package com.example.RSW.service;

import com.example.RSW.repository.LabResultDetailRepository;
import com.example.RSW.vo.LabResultDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabResultDetailService {

    private final LabResultDetailRepository labResultDetailRepository;
    private final ObjectMapper objectMapper;

    /* ====== 기존 CRUD ====== */
    public int insert(LabResultDetail row){ return labResultDetailRepository.insert(row); }

    // 🔧 int[] → int 로 통일
    public int insertBatch(List<LabResultDetail> list){
        return labResultDetailRepository.insertBatch(list);
    }

    public int update(LabResultDetail row){ return labResultDetailRepository.update(row); }

    public void delete(int id){ labResultDetailRepository.delete(id); }

    public LabResultDetail selectById(int id){ return labResultDetailRepository.selectById(id); }

    public List<LabResultDetail> selectByDocumentId(int documentId){
        return labResultDetailRepository.selectByDocumentId(documentId);
    }

    public List<LabResultDetail> selectByVisitId(int visitId){
        return labResultDetailRepository.selectByVisitId(visitId);
    }

    public List<LabResultDetail> selectByPetId(int petId){
        return labResultDetailRepository.selectByPetId(petId);
    }

    /* =========================================================
       ========== 신규: OCR groups → detail 테이블 저장 ==========
       ========================================================= */

    /**
     * ocr_json에서 groups를 읽어 "가장 최신(날짜가 가장 큰) 그룹"만 펼쳐 저장.
     * 기존 documentId의 행은 전부 삭제 후 일괄 삽입(UPSERT).
     * @return 저장된 로우 수
     */
    @Transactional
    public int upsertLatestGroup(int documentId, String ocrJson) throws Exception {
        // 1) 기존 삭제
        labResultDetailRepository.deleteByDocumentId(documentId);

        // 2) JSON 파싱 → 최신 date 선택
        JsonNode root   = objectMapper.readTree(ocrJson == null ? "{}" : ocrJson);
        JsonNode groups = root.path("groups");
        if (!groups.isArray() || groups.size() == 0) return 0;

        LocalDate latestDate = null;
        JsonNode latestGroup = null;
        for (JsonNode g : groups) {
            String ds = g.path("date").asText(null);
            LocalDate d = parseIsoDate(ds);
            if (d == null) continue;
            if (latestDate == null || d.isAfter(latestDate)) {
                latestDate = d; latestGroup = g;
            }
        }
        if (latestGroup == null) latestGroup = groups.get(0); // 폴백

        // 3) items → rows
        List<LabResultDetail> rows = new ArrayList<>();
        for (JsonNode it : latestGroup.path("items")) {
            if (!it.isObject()) continue;

            String name = nz(it.path("name").asText(null));
            if (name == null) continue;

            Double value  = toD(it.path("value"));
            Double refLow = toD(it.path("ref_low"));
            Double refHigh= toD(it.path("ref_high"));
            String unit   = it.path("unit").asText("");
            String flag   = nz(it.path("flag").asText(null));
            String notes  = nz(it.path("note").asText(null));

            LabResultDetail r = new LabResultDetail();
            r.setDocumentId(documentId);
            r.setResultDate(latestDate);     // ✅ 최신 날짜 저장 (result_date)
            r.setTestName(name);
            r.setResultValue(value);
            r.setUnit(unit);
            r.setRefLow(refLow);
            r.setRefHigh(refHigh);
            r.setFlag(flag);
            r.setNotes(notes);
            rows.add(r);
        }

        if (rows.isEmpty()) return 0;

        // 4) 일괄 삽입
        int affected = labResultDetailRepository.insertBatch(rows);
        // 일부 드라이버는 0을 돌려줄 수 있으니 rows.size()로 보정 가능
        return affected > 0 ? affected : rows.size();
    }

    /**
     * 모든 그룹을 저장하고 싶을 때 사용하는 보조 메서드(옵션).
     */
    @Transactional
    public int upsertAllGroups(int documentId, String ocrJson) {
        List<LabResultDetail> rows = extractAllGroupsAsRows(documentId, ocrJson);
        labResultDetailRepository.deleteByDocumentId(documentId);
        if (rows.isEmpty()) return 0;

        int affected = 0;
        try {
            affected = labResultDetailRepository.insertBatch(rows);
        } catch (Exception e) {
            // 배치 실패시 단건 삽입 폴백
            for (LabResultDetail r : rows) affected += labResultDetailRepository.insert(r);
        }
        return affected > 0 ? affected : rows.size();
    }

    /* ================= 내부 파서 ================= */

    private List<LabResultDetail> extractAllGroupsAsRows(int documentId, String ocrJson) {
        JsonNode groups = readGroups(ocrJson);
        if (groups == null) return List.of();

        List<LabResultDetail> out = new ArrayList<>();
        for (JsonNode g : groups) out.addAll(mapGroup(documentId, g));
        return out;
    }

    private JsonNode readGroups(String ocrJson) {
        try {
            JsonNode root = (ocrJson == null || ocrJson.isBlank())
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(ocrJson);
            JsonNode groups = root.path("groups");
            return (groups != null && groups.isArray()) ? groups : null;
        } catch (Exception e) { return null; }
    }

    private List<LabResultDetail> mapGroup(int documentId, JsonNode groupNode) {
        JsonNode items = groupNode.path("items");
        if (items == null || !items.isArray() || items.size() == 0) return List.of();

        LocalDate groupDate = parseIsoDate(s(groupNode.get("date"))); // ✅ 그룹 날짜 함께 저장
        List<LabResultDetail> out = new ArrayList<>(items.size());

        for (JsonNode it : items) {
            LabResultDetail row = new LabResultDetail();
            row.setDocumentId(documentId);
            row.setResultDate(groupDate);              // ✅
            row.setTestName(s(it.get("name")));
            row.setResultValue(d(it.get("value")));
            row.setUnit(s(it.get("unit")));
            Double lo = d(it.get("ref_low"));
            Double hi = d(it.get("ref_high"));
            row.setRefLow(lo);
            row.setRefHigh(hi);

            String flag = s(it.get("flag"));
            if (flag == null || flag.isBlank()) flag = calcFlag(row.getResultValue(), lo, hi);
            row.setFlag(flag);

            String notes = s(it.get("note"));
            if (notes == null || notes.isBlank()) notes = s(it.get("refRange"));
            row.setNotes(notes);

            if (row.getTestName() == null || row.getTestName().isBlank()) continue;
            out.add(row);
        }

        // 같은 testName은 마지막 값만
        Map<String, LabResultDetail> dedup = out.stream()
                .collect(Collectors.toMap(
                        LabResultDetail::getTestName,
                        r -> r,
                        (oldV, newV) -> newV,
                        LinkedHashMap::new
                ));
        return new ArrayList<>(dedup.values());
    }

    /* ====== 헬퍼 ====== */
    private static LocalDate parseIsoDate(String s){
        if (s == null || s.isBlank()) return null;
        try {
            return (s.length() > 10) ? LocalDate.parse(s.substring(0,10)) : LocalDate.parse(s);
        } catch (Exception ignore) { return null; }
    }

    private static Double toD(JsonNode n){
        try {
            if (n == null || n.isNull()) return null;
            if (n.isNumber()) return n.asDouble();
            String s = n.asText(null);
            if (s == null || s.isBlank()) return null;
            return Double.valueOf(s.replace(",", ""));
        } catch (Exception e) { return null; }
    }

    private static String nz(String s){ return (s == null || s.isBlank()) ? null : s.trim(); }

    private static String calcFlag(Double val, Double lo, Double hi) {
        if (val == null || lo == null || hi == null) return null;
        if (val < lo) return "L";
        if (val > hi) return "H";
        return "N";
    }

    private static String s(JsonNode n) { return (n==null || n.isNull()) ? null : n.asText(); }
    private static Double d(JsonNode n) {
        if (n==null || n.isNull()) return null;
        try { return Double.valueOf(n.asText()); } catch (Exception e) { return null; }
    }
}
