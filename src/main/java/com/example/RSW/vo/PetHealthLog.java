package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetHealthLog {
    private int id;
    private int petId;
    private LocalDateTime logDate;
    private BigDecimal foodWeight;
    private BigDecimal waterWeight;
    private int litterCount;
    private int soundLevel;
    private String notes;
}
