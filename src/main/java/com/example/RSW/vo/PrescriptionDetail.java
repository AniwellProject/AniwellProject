package com.example.RSW.vo;

import lombok.Data;

@Data
public class PrescriptionDetail {
    private int id;
    private int documentId;     // medical_document.id (doc_type='prescription')
    private String drugName;
    private Double doseValue;    // DECIMAL -> Double/BigDecimal 중 택1
    private String doseUnit;     // 'mg','g','mL','IU','tablet','drop'
    private Double freqPerDay;   // 하루 횟수
    private Integer durationDays;
    private String notes;
}
