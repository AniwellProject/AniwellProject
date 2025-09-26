package com.example.RSW.service;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;

public class KakaoPlaceCrawler {

    public static Map<String, Object> crawlPlace(String url) {
        Map<String, Object> result = new HashMap<>();

        // 컨테이너/운영에서 Selenium 끄는 플래그 (원하면 docker run -e SELENIUM_ENABLED=false 로 제어)
        String flag = System.getenv().getOrDefault("SELENIUM_ENABLED", "true");
        boolean seleniumEnabled = flag.equalsIgnoreCase("true");

        // 기본 응답(프론트에서 fallback 로직 있음)
        result.put("status", "");
        result.put("openHour", "");
        result.put("address", "");
        result.put("photos", Collections.emptyList());

        if (!seleniumEnabled) {
            result.put("note", "selenium_disabled");
            return result; // ✅ 바로 우회: 500 발생 안 함
        }

        ChromeOptions options = new ChromeOptions();
        // 컨테이너/헤드리스에서 안전한 옵션들
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);   // ❗컨테이너엔 chromedriver가 없어서 여기서 터졌던 것
            driver.get(url);

            // TODO: 여기 기존의 findElement(...) 들 그대로 유지
            // 예: result.put("status", safeText(driver, By.cssSelector("...")));
            //     result.put("openHour", safeText(driver, By.cssSelector("...")));
            //     result.put("address", safeText(driver, By.cssSelector("...")));
            //     result.put("photos", photosList);

            return result;

        } catch (Throwable t) {
            // ✅ 실패해도 500 내지 말고 기본 응답 반환 (프런트는 place 객체로 보완 표시)
            result.put("note", "selenium_unavailable");
            return result;

        } finally {
            if (driver != null) {
                try { driver.quit(); } catch (Exception ignore) {}
            }
        }
    }

    private static String safeText(WebDriver driver, By selector) {
        try {
            WebElement el = driver.findElement(selector);
            return el.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
