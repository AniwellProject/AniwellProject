package com.example.RSW.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LabResultDetail {
    private int id;
    private int documentId;     // medical_document.id (doc_type='lab')
    private String testName;     // 검사 항목명
    private Double resultValue;  // DECIMAL(12,4) -> Double/BigDecimal 중 택1
    private String unit;
    private Double refLow;
    private Double refHigh;
    private String flag;         // 'L','N','H'
    private String notes;

    private LocalDate resultDate;
}
