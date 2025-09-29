package com.example.RSW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication  
public class AniwellProjectApplication {


    public static void main(String[] args) {
        SpringApplication.run(AniwellProjectApplication.class, args);
    }
}
