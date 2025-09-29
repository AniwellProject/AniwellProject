package com.example.RSW.controller;

import com.example.RSW.dto.PetHealthLogDto;
import com.example.RSW.service.PetHealthService;
import com.example.RSW.vo.PetHealthLog;
import com.example.RSW.vo.ResultData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class PetHealthController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private PetHealthService healthService;

	// ✅ ESP32나 외부에서 DTO로 저장 + WebSocket 브로드캐스트
	@PostMapping("/usr/pet/health/log")
	@ResponseBody
	public ResultData saveFromEsp(@RequestBody PetHealthLogDto dto) {
		PetHealthLog saved = healthService.save(dto);
		messagingTemplate.convertAndSend("/topic/health/" + saved.getPetId(), saved);
		return ResultData.from("S-1", "저장됨");
	}

	// ✅ 클라이언트에서 직접 저장 + WebSocket 브로드캐스트
	@PostMapping("/usr/pet/health/save")
	@ResponseBody
	public ResultData saveFromClient(@RequestBody PetHealthLog log) {
		healthService.save(log);
		messagingTemplate.convertAndSend("/topic/health/" + log.getPetId(), log);
		return ResultData.from("S-1", "저장 완료");
	}

	// ✅ 건강 로그 조회 페이지 렌더링 + JSON 문자열 전달
	@GetMapping("/usr/pet/health")
	public String showLogs(@RequestParam("petId") int petId, Model model) {
		List<PetHealthLog> logs = healthService.getLogsByPetId(petId);
		model.addAttribute("logs", logs);
		model.addAttribute("petId", petId);

		ObjectMapper objectMapper = new ObjectMapper();
		String logsJson = "[]";
		try {
			logsJson = objectMapper.writeValueAsString(logs);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		model.addAttribute("logsJson", logsJson);

		return "usr/pet/health";
	}

	@GetMapping("/usr/pet/health/logs")
	@ResponseBody
	public List<PetHealthLog> getLogsByDate(@RequestParam int petId, @RequestParam String date) {
		LocalDate targetDate = LocalDate.parse(date);
		return healthService.getLogsByPetIdAndDate(petId, targetDate);
	}

	@GetMapping("/usr/pet/health/week-stats")
	@ResponseBody
	public Map<String, Object> getWeekStats(@RequestParam int petId) {
		return healthService.getWeeklyChartData(petId);
	}

	// ✅ [추가] OCR 업로드 페이지(Thymeleaf 템플릿) 열기
//  - 주소창으로 접근: GET /usr/pet/ocr
//  - 템플릿: /templates/usr/pet/ocr.html
	@GetMapping("/usr/pet/ocr")
	public String petOcrPage(@RequestParam(value = "petId", required = false) Integer petId, // 선택: 펫ID 넘겨받아 화면에서 활용
			Model model) {
		// 주석: 페이지에서 필요 시 펫ID로 OCR 결과를 해당 펫 기록에 연결할 수 있음
		model.addAttribute("petId", petId);
		return "usr/pet/ocr"; // ⬅ yml의 prefix/suffix 설정 기준으로 /templates/usr/pet/ocr.html 렌더링
	}
}
