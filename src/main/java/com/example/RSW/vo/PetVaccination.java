package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetVaccination {
    private int id;
    private int petId;
    private String vaccineName;
    private Date injectionDate;
    private Date nextDueDate;
    private String vetName;
    private String notes;

    // 조인 정보 (선택)
    private String petName;
    private String species;
    private String breed;
}
