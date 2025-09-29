// ✅ OcrSaveVo.java
package com.example.RSW.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrSaveVo {

	// ❌ [변경] 더 이상 필수 아님 (프런트에서 안 보낼 수도 있음)
	private String text;

	// ✅ [신규] OCR 파서가 만든 날짜별 그룹 리스트 (이걸 저장 대상)
	private List<Map<String, Object>> groups;

	// (선택) 엔진/타임스탬프 등 OCR 메타
	private Map<String, Object> ocrMeta;

	// [선택] 기존 방문에 붙일 때
	private Integer visitId;

	// [선택] 새 방문 생성용
	private Integer petId;           // visitId 없으면 필요
	private LocalDateTime visitDate;
	private String hospital;
	private String doctor;
	private String diagnosis;
	private String notes;
	private BigDecimal totalCost;

	private String docType; // receipt | prescription | lab | diagnosis | other
	private String fileUrl; // 원본 파일 URL

	// 편의 메서드
	public boolean hasGroups() { return groups != null && !groups.isEmpty(); }
	public boolean hasText()   { return text != null && !text.isBlank(); }
}
