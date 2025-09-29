package com.example.RSW.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class LitterAnalyzeResponse<T> {
    private String code;     // "OK" | "IGNORED" | "ERROR"
    private String message;
    private T data;
}
