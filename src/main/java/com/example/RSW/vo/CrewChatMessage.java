package com.example.RSW.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrewChatMessage {
    private int crewId;
    private int senderId;
    private String content;
    private LocalDateTime sentAt;

    private String nickname;
}
