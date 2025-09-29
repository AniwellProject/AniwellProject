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
public class PetBleActivity {
    private int id;  // PK
    private int petId;
    private String zoneName;
    private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
    private int durationSec;
    private int rssi;
}
