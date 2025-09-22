package com.example.RSW.service;

import com.example.RSW.dto.ExplainRequest;
import com.example.RSW.vo.Pet;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/** Pet 엔터티 → ExplainRequest.Pet 변환 어댑터 */
public final class PetInfoAdapter {
    private PetInfoAdapter() {}

    /** ref: 문서 기준 날짜(없으면 today) */
    public static ExplainRequest.Pet toExplainPet(Pet p, LocalDate ref) {
        if (p == null) {
            return new ExplainRequest.Pet(null, null, null, null, null, null);
        }

        LocalDate birth = toLocalDate(p.getBirthDate()); // <-- 안전 변환
        Integer years = null;
        Integer months = null;

        if (birth != null) {
            if (ref == null) ref = LocalDate.now();
            if (ref.isBefore(birth)) {
                years = 0;
                months = 0;
            } else {
                Period period = Period.between(birth, ref);
                years  = Math.max(0, period.getYears());
                months = Math.max(0, period.getMonths());
            }
        }

        return new ExplainRequest.Pet(
                p.getName(),
                mapSpecies(p.getSpecies()),
                years,                          // 생일 없으면 null
                mapSex(p.getGender()),
                months,                         // 개월(0~11) 또는 null
                (birth != null ? birth.toString() : null)
        );
    }

    /** Date 타입 안전 변환 (java.sql.Date / Timestamp / util.Date 모두 지원) */
    private static LocalDate toLocalDate(Date d){
        if (d == null) return null;

        // java.sql.Date: toInstant() 금지, 전용 메서드 사용
        if (d instanceof java.sql.Date) {
            return ((java.sql.Date) d).toLocalDate();
        }
        // java.sql.Timestamp
        if (d instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) d).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }
        // java.util.Date
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String mapSex(String g){
        if (g == null) return null;
        String s = g.trim().toUpperCase();
        if ("M".equals(s) || "MALE".equals(s)) return "M";
        if ("F".equals(s) || "FEMALE".equals(s)) return "F";
        return s;
    }

    private static String mapSpecies(String s){
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        if ("cat".equals(t) || "고양이".equals(t)) return "고양이";
        if ("dog".equals(t) || "개".equals(t) || "강아지".equals(t)) return "개";
        return s.trim();
    }
}
