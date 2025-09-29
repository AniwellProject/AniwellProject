package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetAnalysis {
    private int id;
    private int petId;
    private String imagePath;
    private String emotionResult;
    private double confidence;
    private LocalDateTime analyzedAt;
}
