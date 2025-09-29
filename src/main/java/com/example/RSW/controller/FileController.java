package com.example.RSW.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {

    @GetMapping("/gen/file/download")
    public void downloadFile(@RequestParam("url") String fileUrl, HttpServletResponse response) {
        try {
            // 1. 로그 찍기
            System.out.println("📥 다운로드 요청 URL: " + fileUrl);

            // 2. 파일 이름 추출 및 인코딩
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");

            // 3. 응답 헤더 설정
            response.setHeader("Content-Disposition", "inline; filename=\"" + encodedFilename + "\"");
            response.setContentType("application/octet-stream");

            // 4. URL로부터 InputStream 열고 복사
            try (InputStream in = new URL(fileUrl).openStream()) {
                FileCopyUtils.copy(in, response.getOutputStream());
            }

        } catch (Exception e) {
            System.err.println("❌ 다운로드 실패: " + e.getMessage());
            e.printStackTrace();

            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


}
