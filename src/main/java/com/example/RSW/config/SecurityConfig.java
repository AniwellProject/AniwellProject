package com.example.RSW.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order; // ✅ JUnit 말고 이거!
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    @Order(-10) // ✅ 모든 체인보다 앞
    public SecurityFilterChain litterOnlyChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/api/litter/**")
                        // 프록시 프리픽스까지 잡고 싶으면 ↓ 한 줄 추가해도 됨
                        // , new AntPathRequestMatcher("/**/api/litter/**")
                ))
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // 디바이스/멀티파트 업로드 편의
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    /**
     * ===== B) API 체인: /api/** =====
     */
    @Bean
    @Order(0)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/verify/**").permitAll()   // ✅ send/check 전체 허용
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/pet/report",
                                "/api/pet/weight-timeline"
                        ).permitAll()
                        .requestMatchers("/api/verify/sms/**").permitAll()
                        .requestMatchers("/api/pet/**").authenticated()
                        .requestMatchers("/api/member/**").authenticated()
                        .anyRequest().permitAll()
                )
                // ✅ 인증이 비어 있어도 익명 접근 허용
                .anonymous(Customizer.withDefaults())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/usr/**", "/api/**"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(sessionFixation -> sessionFixation.none())
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ✅ 세이프가드: 혹시 앱 체인으로 흘러와도 /api/** 통과
                        .requestMatchers("/api/**").permitAll()
                        /* 정적/공개 리소스 */
                        .requestMatchers(
                                "/", "/usr/home/main", "/usr/member/doModify",
                                "/usr/member/login", "/usr/member/doLogin",
                                "/usr/member/join", "/usr/member/doJoin",
                                "/usr/member/doFindLoginId", "/usr/member/doFindLoginPw",
                                "/usr/member/findLoginId", "/usr/member/findLoginPw",
                                "/usr/member/doModify", "/usr/member/modify",
                                "/usr/member/naver/**", "/usr/member/kakao/**", "/usr/member/google/**",
                                "/usr/member/social-login",
                                "/usr/member/firebase-session-login",
                                "/usr/member/getLoginIdDup",
                                "/usr/member/getEmailDup",
                                "/usr/member/getNicknameDup",
                                "/usr/member/getCellphoneDup",
                                "/css/**", "/js/**", "/img/**", "/img.socialLogin/**",
                                "/resource/**",
                                "/favicon.ico"   // ✅ favicon.ico 명시적으로 허용
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/usr/pet/daily/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/usr/pet/daily/**").permitAll()
                        .anyRequest().authenticated()
                )
                // ✅ app 체인으로 들어온 /api/** 가 보호 자원으로 인식되어도 리다이렉트 방지
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(login -> login
                        .loginPage("/usr/member/login")
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/usr/member/doLogout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        // ✅ remember-me 쿠키도 함께 제거
                        .deleteCookies("JSESSIONID", "REMEMBER_ME")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeCookieName("REMEMBER_ME")          // 쿠키 이름 명시
                        .tokenValiditySeconds(14 * 24 * 60 * 60)      // 14일 유지
                        .alwaysRemember(true)                         // 체크박스 없어도 자동 발급
                        .useSecureCookie(true)                        // HTTPS 환경에서만 전송
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "https://aniwell.s3.ap-northeast-2.amazonaws.com",
                "http://aniwell.s3-website.ap-northeast-2.amazonaws.com",
                "http://localhost:3001",
                "http://localhost:8080"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Location"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }
}