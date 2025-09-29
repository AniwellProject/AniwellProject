package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    private int id;
    private Integer memberId; // 회원 ID
    private String title;     // 알림 제목
    private String link;      // 알림 링크
    private Date regDate;// 등록일

    private boolean isRead;        // 읽음 여부
    private String timeAgo;       // 클라이언트 표시용 (필요시)


    private String type;        // 알림 종류 ("LIKE", "COMMENT", "REPLY" 등)
    private Integer senderId;   // 알림 발생 유저 ID



    public long getRegDateMs() {
        return regDate != null ? regDate.getTime() : 0L;
    }
}
