package com.example.RSW.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetBleActivityDto {
    private int petId;
    private String zoneName;
    private String enteredAt;  // ex) "2025-07-15T10:00:00"
    private String exitedAt;
    private int durationSec;
    private int rssi;
}
