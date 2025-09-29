package com.example.RSW.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class AppConfig {
	@Value("${kakao.rest-api-key}")
	private String kakaoRestApiKey;

	@Value("${kakao.javascript-key}")
	private String kakaoJavascriptKey;

	public String getKakaoRestApiKey() {
		return kakaoRestApiKey;
	}

	public String getKakaoJavascriptKey() {
		return kakaoJavascriptKey;
	}
}
