package com.example.RSW.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class WeightPoint {
    private String measuredAt;     // ← 날짜 문자열로 받아두면 타입핸들러 이슈 배제
    private BigDecimal weightKg;
    private String foodType;
    private String brand;
    private String productName;
    private String flavor;
    private String lifeStage;
}

