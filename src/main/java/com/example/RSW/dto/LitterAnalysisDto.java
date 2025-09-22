package com.example.RSW.dto;

import lombok.Data;
import java.util.List;

@Data
public class LitterAnalysisDto {
    private String type;                 // pee | poop | unknown
    private Double confidence;
    private List<String> visual_signals;
    private String notes;
    private List<String> anomalies;
    private Boolean ignored;             // 프리필터로 무시 시 true
    private String ignoreReason;
}