package com.example.RSW.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalkCrew {

	private int id; // 크루 ID
	private String title; // 크루 제목
	private String description; // 크루 설명
	private int districtId; // district 테이블의 id (FK)
	private int leaderId; // 작성자 ID
	private LocalDateTime createdAt; // 생성일시
	private String imageUrl;

	// JOIN 조회용 필드 (optional)
	private String nickname;
	private String city; // 시
	private String district; // 구
	private String dong; // 동
}