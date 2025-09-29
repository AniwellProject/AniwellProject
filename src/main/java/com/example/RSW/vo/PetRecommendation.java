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
public class PetRecommendation {

    private int id;
    private int memberId;
    private String type;     // '병원' or '용품'
    private String name;
    private String address;
    private String phone;
    private String mapUrl;
    private LocalDateTime createdAt;

}
