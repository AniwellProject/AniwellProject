package com.example.RSW.vo;

import lombok.Data;

/*지역 불러오기 용으로 필요*/
@Data
public class District {
	private int id;
	private String sido; // 시/도
	private String sigungu; // 구/군
	private String dong; // 동
	private String fullName; // 전체 주소 문자열
	private String code; // 법정동 코드
}
