package com.example.RSW.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetHealthLogDto {
    private int petId;
    private String logDate;  // "2025-07-15T07:55:00" 형태
    private BigDecimal foodWeight;
    private BigDecimal waterWeight;
    private int litterCount;
    private int soundLevel;
    private String notes;
}
