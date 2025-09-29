package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetFeedLog {
	private int id;
	private int petId;
	private Timestamp fedAt; // 급여 시각
	private String brand; // 급여 당시 브랜드 (대표사료일 수도, 아닐 수도)
	private String feedType; // "dry"/"wet"
	private Double amountG; // 선택: 급여량(g) - NOT NULL이면 0.0 기본값 처리
	private String foodName;
	private String source; // "manual" 등
	private String note; // 비고
	private Timestamp regDate;
	private Timestamp updateDate;

}
