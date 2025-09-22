package com.example.RSW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication  // ← 이 애노테이션이 핵심입니다!
public class AniwellProjectApplication {


    public static void main(String[] args) {
        SpringApplication.run(AniwellProjectApplication.class, args);
    }
}
