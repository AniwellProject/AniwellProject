package com.example.RSW.service;

import com.example.RSW.dto.PetHealthLogDto;
import com.example.RSW.repository.PetHealthRepository;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.PetHealthLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PetHealthService {

    @Autowired
    private PetHealthRepository repo;

    @Autowired
    PetService petService;

    // ✅ Dto를 VO로 변환해 저장하고, 저장된 VO 반환
    public PetHealthLog save(PetHealthLogDto dto) {
        PetHealthLog log = PetHealthLog.builder()
                .petId(dto.getPetId())
                .logDate(LocalDateTime.parse(dto.getLogDate()))
                .foodWeight(dto.getFoodWeight())
                .waterWeight(dto.getWaterWeight())
                .litterCount(dto.getLitterCount())
                .soundLevel(dto.getSoundLevel())
                .notes(dto.getNotes())
                .build();

        repo.insertLog(log);
        return log;  // WebSocket 브로드캐스트용
    }

    // ✅ VO 자체 저장도 가능하게
    public PetHealthLog save(PetHealthLog log) {
        repo.insertLog(log);
        return log;
    }

    public List<PetHealthLog> getLogsByPetId(int petId) {
        return repo.findLogsByPetId(petId);
    }

    public List<PetHealthLog> getLogsByPetIdAndDate(int petId, LocalDate date) {
        return repo.findByPetIdAndDate(petId, date);
    }

    public Map<String, Object> getWeeklyChartData(int petId) {
        List<Map<String, Object>> dbResults = repo.getWeeklyStats(petId);  // 쿼리 호출

        // 🐾 고양이 몸무게 가져오기
        Pet pet = petService.getPetsById(petId);
        double weight = pet.getWeight();

        // 📅 요일 라벨 (Chart.js용)
        List<String> labels = List.of("월", "화", "수", "목", "금", "토", "일");

        // 📊 요일별 점수 초기화
        double[] foodScore = new double[7];   // 월=0 ~ 일=6
        double[] waterScore = new double[7];

        for (Map<String, Object> row : dbResults) {
            int dayIndex = ((Number) row.get("dayOfWeek")).intValue();  // 0=월 ~ 6=일

            double foodTotal = row.get("foodTotal") != null ? ((Number) row.get("foodTotal")).doubleValue() : 0.0;
            double waterTotal = row.get("waterTotal") != null ? ((Number) row.get("waterTotal")).doubleValue() : 0.0;

            // ✅ 몸무게 기준 점수 계산
            foodScore[dayIndex] = calculateScore(foodTotal, weight * 15.0, weight * 20.0);     // 권장 사료량 범위
            waterScore[dayIndex] = calculateScore(waterTotal, weight * 50.0, weight * 70.0);   // 권장 물 섭취량
        }

        // ✅ 평균 점수 계산 (소수점 1자리 반올림)
        List<Double> avgScore = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            double avg = (foodScore[i] + waterScore[i]) / 2.0;
            avgScore.add(Math.round(avg * 10.0) / 10.0);
        }

        // ✅ 결과 구성
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("food", Arrays.stream(foodScore).boxed().toList());
        result.put("water", Arrays.stream(waterScore).boxed().toList());
        result.put("score", avgScore);
        System.out.println(result);


        return result;
    }


    private double calculateScore(double actual, double targetMin, double targetMax) {
        double targetMid = (targetMin + targetMax) / 2.0;

        if (actual >= targetMin && actual <= targetMax) return 10.0;

        double ratio = actual / targetMid;
        double score;

        if (ratio < 0.8) {
            score = 10.0 * (ratio / 0.8);
        } else if (ratio > 1.2) {
            score = 10.0 * ((2.0 - ratio) / 0.8);
        } else {
            score = 10.0;
        }

        return Math.max(0.0, Math.min(10.0, Math.round(score * 10.0) / 10.0));
    }


}
