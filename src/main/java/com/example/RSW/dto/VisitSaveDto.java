// [추가파일] src/main/java/com/example/RSW/api/dto/VisitSaveDto.java
package com.example.RSW.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class VisitSaveDto {

	@Data
	public static class VisitDto {
		private Integer petId; // visit.pet_id
		private OffsetDateTime visitDate; // visit.visit_date
		private String hospital;
		private String doctor;
		private String diagnosis;
		private String notes;
		private BigDecimal totalCost;
	}

	@Data
	public static class LabResultDto {
		private String testName;
		private BigDecimal resultValue;
		private String unit;
		private BigDecimal refLow;
		private BigDecimal refHigh;
		private String flag; // L | N | H (없으면 서버가 계산)
		private String notes;
	}

	@Data
	public static class PrescriptionDto {
		private String drugName;
		private BigDecimal doseValue;
		private String doseUnit; // 'mg','g','mL','IU','tablet','drop'
		private BigDecimal freqPerDay;
		private Integer durationDays;
		private String notes;
	}

	@Data
	public static class DocumentDto {
		private String docType; // receipt | prescription | lab | diagnosis
		private String fileUrl;
		private Object ocrJson; // JSON 객체 → DB에는 문자열로 저장
		private List<LabResultDto> labResults;
		private List<PrescriptionDto> prescriptions;
	}

	private VisitDto visit;
	private List<DocumentDto> documents;
}
