package com.example.RSW.vo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Article {

    private int id;
    private String regDate;
    private String updateDate;
    private int memberId;
    private int boardId;
    private String title;
    private String body;
    private int hitCount;
    private int goodReactionPoint;
    private int badReactionPoint;
    private Integer crewId;

    private String imageUrl;
    private Date scheduleDate;

    private String extra__writer;

    private String extra__repliesCount;

    private String extra__sumReactionPoint;

    private boolean userCanModify;
    private boolean userCanDelete;
}