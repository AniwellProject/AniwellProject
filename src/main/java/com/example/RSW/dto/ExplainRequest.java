package com.example.RSW.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM으로 넘길 표준 입력 형태
 * - document(기관/날짜)
 * - pet(이름/종/나이/성별)  ※ 펫 정보는 별도 테이블에서 읽어 매핑
 * - labs(검사 결과 배열)
 * - prescriptions(처방 배열)
 * - meta(메모)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplainRequest {
    private Document document;
    private Pet pet;
    private List<Lab> labs;
    private List<Prescription> prescriptions;
    private Meta meta;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Document {
        private String provider;   // 병원/기관명
        private String date;       // 문서 날짜 (예: "2025-08-10")
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Pet {
        private String name;
        private String species;
        private Integer age;        // 년(만 연세)
        private String sex;
        private Integer ageMonths;  // 개월(0..11)  ← 추가
        private String birthDate;   // "yyyy-MM-dd" (표시/디버그용, 선택)
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Lab {
        private String code;       // 검사 코드/이름
        private String name;       // (선택) 표시 이름
        private Double value;      // 결과값
        private String unit;       // 단위
        private Double ref_low;    // 참고범위 하한
        private Double ref_high;   // 참고범위 상한
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Prescription {
        private String drug;       // 약품명
        private Double dose;       // 용량 값
        private String dose_unit;  // 용량 단위 (mg 등)
        private String freq;       // 빈도 (BID 등)
        private String route;      // 경로 (PO 등)
        private Integer days;      // 일수
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Meta {
        private String notes;      // 메모/참고
    }
}
