package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CalendarEvent {
    private int id;
    private int memberId;
    private Integer petId;
    private LocalDate eventDate;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
