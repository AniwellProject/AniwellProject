// ✅ [추가] Google Cloud Vision 기반 OCR 서비스
//    - 역할: 업로드 이미지 → GCV 호출 → { text, confidence:null } JSON(Map) 반환
//    - 장점: 컨트롤러를 얇게 유지, 추후 전처리/로깅/통계 확장 용이

package com.example.RSW.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

// ⬇️ Google Cloud Vision SDK
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.auth.oauth2.GoogleCredentials; // ★ 새로 필요
import com.google.auth.oauth2.ServiceAccountCredentials; // ★ 새로 필요
import com.google.api.gax.core.FixedCredentialsProvider; // ★ setCredentialsProvider에 필요

//✅ [추가] 언어 힌트용
import com.google.cloud.vision.v1.ImageContext;

//✅ [추가 - 선택] 타임아웃/재시도 설정용
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import java.time.Duration; // (컴파일 오류 시 org.threeten.bp.Duration 으로 교체)
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class OcrService {

	private final ResourceLoader resourceLoader;

	public OcrService(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Value("${gcv.credentials.path:}")
	private String gcvPath;

	@Value("${gcv.credentials.base64:}")
	private String gcvBase64;

	// ✅ OCR 모드 설정 (기본: 문서/영수증에 적합한 DOCUMENT_TEXT_DETECTION)
	@Value("${gcv.ocrMode:DOCUMENT_TEXT_DETECTION}")
	private String gcvOcrMode;

	/**
	 * 이미지에서 텍스트를 추출하여 React에서 쓰기 좋은 JSON(Map)으로 반환
	 * 
	 * @param file 업로드 이미지(Multipart)
	 * @return { "text": "...", "confidence": null }
	 * @throws Exception GCV 호출/IO 중 예외 발생 시 상위에서 처리
	 */
	public Map<String, Object> extractText(MultipartFile file) throws Exception {
		// 1) 업로드 파일 바이트 → GCV Image 변환
		// ✅ [교체] 파일 검증 + 안전한 바이트 로드
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("이미지 파일이 비어있습니다."); // 입력값 보호
		}
		ByteString imgBytes = ByteString.copyFrom(file.getBytes()); // 스트림 대신 바이트 배열 사용(리소스 안전)
		Image img = Image.newBuilder().setContent(imgBytes).build(); // GCV Image 생성(동일)

		// 2) OCR 모드 선택: TEXT_DETECTION(간판/짧은문구) vs DOCUMENT_TEXT_DETECTION(영수증/문서)
		Type type = "TEXT_DETECTION".equalsIgnoreCase(gcvOcrMode) ? Feature.Type.TEXT_DETECTION
				: Feature.Type.DOCUMENT_TEXT_DETECTION; // 기본: 문서형

		// 3) 기능(Feature) 지정 및 요청 생성
		// ✅ [교체] 언어 힌트(ko/en) 추가하여 인식률 개선
		Feature feat = Feature.newBuilder().setType(type).build();

		// ⬇️ 추가: 이미지 컨텍스트에 언어 힌트 세팅
		ImageContext ctx = ImageContext.newBuilder().addLanguageHints("ko") // 한국어
				.addLanguageHints("en") // 영어 혼용 대비
				.build();

		AnnotateImageRequest req = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img)
				.setImageContext(ctx) // ← 힌트 적용
				.build();

		String text;

		// 4) GCV 클라이언트로 배치 요청(여기서는 단일 이미지만 전송)
		try (ImageAnnotatorClient client = createVisionClient(gcvPath, gcvBase64)) {
			BatchAnnotateImagesResponse resp = client.batchAnnotateImages(java.util.List.of(req));
			AnnotateImageResponse r = resp.getResponses(0);

			// 5) 오류 처리
			if (r.hasError()) {
				throw new IllegalStateException("Vision OCR error: " + r.getError().getMessage());
			}

			// 6) 문서형이면 fullTextAnnotation.text, 일반형이면 textAnnotations[0].description 사용
			if (r.hasFullTextAnnotation()) {
				text = r.getFullTextAnnotation().getText();
			} else if (!r.getTextAnnotationsList().isEmpty()) {
				text = r.getTextAnnotations(0).getDescription();
			} else {
				text = "";
			}
		}

		// 7) React 친화 JSON(Map) 구성
		Map<String, Object> payload = new HashMap<>();
		payload.put("text", text != null ? text.trim() : "");
		payload.put("confidence", null); // 평균 신뢰도는 기본 응답에 별도 제공되지 않음(필요시 확장)
		return payload;
	}

	// ✅ GCV Credentials 로딩 메서드 추가
	private GoogleCredentials loadGcvCredentials(String path, String base64) throws IOException {
		// base64 → 우선 사용
		if (base64 != null && !base64.isBlank()) {
			byte[] jsonBytes = Base64.getDecoder().decode(base64);
			try (var in = new ByteArrayInputStream(jsonBytes)) {
				return ServiceAccountCredentials.fromStream(in);
			}
		}
		// path → file:/ or classpath:/ 지원
		if (path != null && !path.isBlank()) {
			Resource r = resourceLoader.getResource(path);
			try (var in = r.getInputStream()) {
				return ServiceAccountCredentials.fromStream(in);
			}
		}
		// 둘 다 없으면 ADC
		return GoogleCredentials.getApplicationDefault();
	}

	// ✅ ImageAnnotatorClient 생성 메서드 (Credentials 적용)
	private ImageAnnotatorClient createVisionClient(String path, String base64) throws IOException {
		GoogleCredentials credentials = loadGcvCredentials(path, base64);

		// ✅ 명시적으로 고정 Provider 사용 (버전 차이에 안전)
		ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
				.setCredentialsProvider(com.google.api.gax.core.FixedCredentialsProvider.create(credentials)) // ← 변경
				.build();

		return ImageAnnotatorClient.create(settings);
	}
}
