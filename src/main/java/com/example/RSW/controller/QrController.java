// QrController.java
package com.example.RSW.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    @Value("${aniwell.qr.base:}")        // 예: https://aniwell.s3.ap-northeast-2.amazonaws.com/qr.html
    private String qrBase;

    @Value("${aniwell.api.default:}")    // 예: https://<trycloudflare-도메인>
    private String defaultApiBase;

    @GetMapping(value="/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> debug(@RequestParam Map<String,String> all, HttpServletRequest req) {
        Map<String,String> q = new LinkedHashMap<>(all);
        q.remove("size");
        ensureApiParam(q, req);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("base", resolveBase(req));
        out.put("apiBase", resolveApiBase(req));
        out.put("params", q);
        out.put("target", buildTarget(resolveBase(req), q));
        return out;
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generate(
            @RequestParam Map<String,String> all,
            @RequestParam(defaultValue = "320") int size,
            HttpServletRequest req
    ) throws Exception {
        Map<String,String> q = new LinkedHashMap<>(all);
        q.remove("size");

        // ✅ 항상 api 파라미터 보장
        ensureApiParam(q, req);

        String target = buildTarget(resolveBase(req), q);
        if (target == null || target.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("target url is empty".getBytes());
        }

        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);

        BitMatrix m = new MultiFormatWriter().encode(target, BarcodeFormat.QR_CODE, size, size, hints);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(m, "PNG", baos);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noCache())
                .body(baos.toByteArray());
    }

    private void ensureApiParam(Map<String,String> q, HttpServletRequest req) {
        if (!q.containsKey("api") || q.get("api") == null || q.get("api").isBlank()) {
            q.put("api", resolveApiBase(req));
        }
    }

    private String resolveBase(HttpServletRequest req) {
        String base = (qrBase == null) ? "" : qrBase.trim();
        if (base.isEmpty()) {
            return ServletUriComponentsBuilder.fromCurrentContextPath().path("/usr/pet/qr").toUriString();
        }
        if (base.startsWith("http://") || base.startsWith("https://")) return base;
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(base.startsWith("/") ? base : "/" + base).toUriString();
    }

    // ⚠️ default가 비었으면 "현재 요청의 오리진(=터널/도메인)"을 자동 사용
    private String resolveApiBase(HttpServletRequest req) {
        if (defaultApiBase != null && !defaultApiBase.isBlank()) return defaultApiBase.trim();
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    private String buildTarget(String base, Map<String,String> q) {
        try {
            UriComponentsBuilder b = UriComponentsBuilder.fromUriString(base);
            q.forEach(b::queryParam);
            return b.build(true).toUriString();
        } catch (Exception e) {
            return null;
        }
    }
}