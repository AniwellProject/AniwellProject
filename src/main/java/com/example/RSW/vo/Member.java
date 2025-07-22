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
public class Member {

	private int id;
	private LocalDateTime regDate;
	private LocalDateTime updateDate;
	private String loginId;
	private String loginPw;
	private String name;
	private String nickname;
	private String cellphone;
	private String email;
	private boolean delStatus;
	private LocalDateTime delDate;
	private int authLevel;
	private String authName;
	private String photo;
	private String address;

	private String vetCertUrl;        // 인증서 경로
	private Integer vetCertApproved;  // 승인 상태

	private String socialProvider; // 예: kakao, google, naver
	private String socialId;       // 소셜 플랫폼 제공 고유 ID

	public boolean isSocialMember() {

		return this.socialProvider != null && !this.socialProvider.isBlank();
	}

}