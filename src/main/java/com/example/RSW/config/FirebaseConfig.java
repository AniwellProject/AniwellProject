package com.example.RSW.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = new ClassPathResource("firebase/aniwell-2a90b-firebase-adminsdk-fbsvc-a0e7397002.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId("aniwell-2a90b") // 🔥 프로젝트 ID 명시
                    .build();

            System.out.println("✅ FirebaseApp 등록됨");
            return FirebaseApp.initializeApp(options);
        }

        System.out.println("✅ 기존 FirebaseApp 반환");
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        // 🔄 firebaseApp이 초기화된 이후에 FirebaseAuth 인스턴스를 가져오기
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
