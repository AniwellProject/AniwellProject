package com.example.RSW.dto;// com.example.RSW.dto.DocEnvelopeDto
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocEnvelopeDto {
    // SELECT 별칭과 1:1
    private Integer documentId;
    private Integer visitId;
    private String  docType;
    private String  fileUrl;
    private String  ocrJson;

    private Integer petId;        // ← column="v_pet_id"
    private LocalDateTime visitDate;
    private String  hospital;
    private String  doctor;
    private String  diagnosis;
    private String  notes;

    private Integer petId2;       // ← column="p_id" (원하면 없애고 petId 하나로 써도 됨)
    private String  petName;
    private String  species;
    private String  sex;          // ← p.gender AS sex
    private String  birthDate;    // ← p.birthDate AS birth_date  (문자열로 받아도 무방)
    private Integer age;          // ← NULL AS age  (없어도 되지만 resultMap에 있으면 DTO에도 있어야 안전)
    private Integer ageMonths;    // ← NULL AS age_months
}
