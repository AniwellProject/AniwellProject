package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalkCrewMember {
	private int id; // ID
	private int crewId; // 크루 ID
	private int memberId; // 회원 ID
	private LocalDateTime joinedAt; // 가입 일시
	private int petId;

	private String role; // 권한: leader, member, subleader 등
	private String status; // ✅ 상태: PENDING, APPROVED 등

	private Member member; // 가입된 멤버 객체

	private String crew_member_id;
	private String crew_member_name;
	private String crew_member_photo;
}
