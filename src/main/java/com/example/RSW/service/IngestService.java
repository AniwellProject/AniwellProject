package com.example.RSW.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class IngestService {

    @Value("${aniwell.ingest.baseDir:/tmp/aniwell_ingest}")
    private String baseDir;

    @Value("${aniwell.ffmpeg.bin:ffmpeg}")
    private String ffmpegBin;

    public Path sessionDir(String session) throws IOException {
        Path dir = Paths.get(baseDir, sanitize(session));
        Files.createDirectories(dir);
        return dir;
    }

    public Path saveJpegFrame(String session, int seq, byte[] jpegBytes) throws IOException {
        Path dir = sessionDir(session);
        String name = String.format(Locale.ROOT, "frame_%04d.jpg", seq);
        Path file = dir.resolve(name);
        Files.write(file, jpegBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return file;
    }

    public Path assembleMp4(String session, int fps) throws IOException, InterruptedException {
        Path dir = sessionDir(session);
        Path out = dir.resolve("clip.mp4");
        // ffmpeg -y -framerate {fps} -i frame_%04d.jpg -pix_fmt yuv420p clip.mp4
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegBin, "-y",
                "-framerate", String.valueOf(fps),
                "-i", "frame_%04d.jpg",
                "-pix_fmt", "yuv420p",
                out.getFileName().toString()
        );
        pb.directory(dir.toFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while (br.readLine() != null) { /* 로그 무시 or 저장 */ }
        }
        int exit = p.waitFor();
        if (exit != 0 || !Files.exists(out)) {
            throw new IOException("ffmpeg failed (exit=" + exit + ")");
        }
        return out;
    }

    public void cleanup(String session) throws IOException {
        Path dir = sessionDir(session);
        if (Files.exists(dir)) {
            // 세션 폴더 통째로 삭제
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                    });
        }
    }

    private String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
