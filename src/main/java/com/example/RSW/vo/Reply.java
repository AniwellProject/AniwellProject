package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reply {

    private int id;
    private String regDate;
    private String updateDate;
    private int memberId;
    private String relTypeCode;
    private int relId;
    private String body;
    private int goodReactionPoint;
    private int badReactionPoint;

    private String extra__writer;

    private String extra__sumReactionPoint;

    private boolean userCanModify;
    private boolean userCanDelete;

    public void setAlreadyLiked(boolean alreadyLiked) {

    }
}