package com.example.RSW.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LitterEvent {
    private Long id;
    private Long petId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime detectedAt;

    private String type;              // pee | poop | unknown
    private Double confidence;
    private String visualSignalsJson; // DB: JSON -> 여기선 문자열로 보관
    private String anomaliesJson;     // DB: JSON -> 여기선 문자열로 보관
    private String notes;
    private String sourceVideo;
    private Long logId;
}
