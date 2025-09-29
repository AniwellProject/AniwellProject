package com.example.RSW.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Visit {
    private int id;
    private int petId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // ✨
    private LocalDateTime visitDate;

    private String hospital;
    private String doctor;
    private String diagnosis;
    private String notes;
    private BigDecimal totalCost;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // (선택)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // (선택)
    private LocalDateTime updatedAt;

}
