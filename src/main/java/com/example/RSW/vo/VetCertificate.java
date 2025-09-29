package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VetCertificate {
    private int id;
    private int memberId;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadedAt;
    private int approved; // 0=대기, 1=승인, 2=거절
}
