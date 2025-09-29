// src/main/java/com/example/RSW/dto/LabDocumentDto.java
package com.example.RSW.dto;

import lombok.Data;

@Data
public class LabDocumentDto {
    private int documentId;     // md.id
    private int visitId;        // v.id
    private String docType;     // 'lab'
    private String storage;     // cloudinary 등
    private String fileUrl;     // md.file_url

    // visit에서 가져오는 추가 정보
    private String visitDate;   // v.visit_date (문자열로 매핑하면 편함)
    private String hospitalName; // v.hospital_name (컬럼명 다르면 XML에서만 바꿔)
}
