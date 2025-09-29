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
public class PetFood {
	private int id;
	private int petId;
	private String brand; // 예: "로얄캐닌"
	private String foodType; // 예: "dry" / "wet"
	private boolean isPrimary; // 1: 대표사료
	private Date startedAt; // 시작일
	private Date endedAt; // 종료일(대표 변경 시 세팅)
	private Timestamp regDate;
	private Timestamp updateDate;

}
