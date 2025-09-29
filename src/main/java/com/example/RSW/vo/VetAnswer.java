package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VetAnswer {
    private int id;
    private int qnaId;
    private int memberId;
    private String vetName;
    private String answer;
    private String answerAt;
}
