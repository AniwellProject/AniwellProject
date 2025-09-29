package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Qna {

    private int id;
    private String title;
    private String body;
    private String regDate;
    private String updateDate;
    private int orderNo;
    private boolean secret;
    private boolean isFromUser;
    private boolean isAnswered;
    private int memberId;
    private boolean isActive;

    private String memberName;

    private boolean hasAnswer;

    private int isFaq;

    private String answer;       // 수의사 답변 내용
    private int answererId;      // 수의사 memberId (원하는 경우)
    private String vetName;      // 수의사 이름

    public boolean isSecret() {
        return secret;
    }
}