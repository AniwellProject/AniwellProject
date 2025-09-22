package com.example.RSW.service;

import com.example.RSW.dto.LitterAnalysisDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PythonRunner {

    private final ObjectMapper om;

    @Value("${aniwell.python.exe}")
    private String pythonExe;

    @Value("${aniwell.python.script}")
    private String scriptPath;

    @Value("${aniwell.python.frames:8}")
    private int defaultFrames;

    @Value("${aniwell.python.model:gpt-4o}")
    private String model;

    @Value("${aniwell.python.ignoreEmpty:true}")
    private boolean ignoreEmpty;

    @Value("${openai.api-key:}")
    private String openaiApiKey;

    /** squatOnly/frames/force 모두 반영하는 메인 엔트리 */
    public LitterAnalysisDto runOnVideo(File video,
                                        Double lingerSec,
                                        boolean force,
                                        Integer framesOverride,
                                        boolean squatOnly) throws Exception {

        int frames = (framesOverride != null ? framesOverride : defaultFrames);

        List<String> cmd = new ArrayList<>();
        cmd.add(pythonExe);
        cmd.add(scriptPath);
        cmd.add("--video");   cmd.add(video.getAbsolutePath());
        cmd.add("--frames");  cmd.add(String.valueOf(frames));
        cmd.add("--model");   cmd.add(model);

        if (lingerSec != null) { cmd.add("--linger"); cmd.add(String.valueOf(lingerSec)); }
        if (squatOnly)        { cmd.add("--squat-only"); }     // ⬅️ 파이썬 스크립트에서 이 플래그 처리
        if (ignoreEmpty && !force) { cmd.add("--ignore-empty"); } // force=true면 프리필터 우회

        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            pb.environment().put("OPENAI_API_KEY", openaiApiKey);
        }
        pb.redirectErrorStream(true);

        Process p = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) out.append(line).append("\n");
        }
        int exit = p.waitFor();
        String stdout = out.toString().trim();

        if (stdout.startsWith("[Ignored]")) {
            LitterAnalysisDto dto = new LitterAnalysisDto();
            dto.setIgnored(true);
            dto.setIgnoreReason(stdout);
            return dto;
        }
        if (exit != 0 && stdout.isEmpty()) {
            throw new RuntimeException("Python process failed (exit=" + exit + ")");
        }
        String json = extractFirstJson(stdout);
        return om.readValue(json, LitterAnalysisDto.class);
    }

    // 기존 analyze 엔드포인트에서 쓰던 오버로드(호환용)
    public LitterAnalysisDto runOnVideo(File video,
                                        Double lingerSec,
                                        boolean force,
                                        Integer framesOverride) throws Exception {
        return runOnVideo(video, lingerSec, force, framesOverride, false);
    }

    private String extractFirstJson(String s) {
        int first = s.indexOf('{'); int last = s.lastIndexOf('}');
        return (first >= 0 && last >= first) ? s.substring(first, last + 1) : s;
    }
}
