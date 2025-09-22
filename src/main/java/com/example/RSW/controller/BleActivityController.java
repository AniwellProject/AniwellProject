package com.example.RSW.controller;

import com.example.RSW.service.PetBleActivityService;
import com.example.RSW.dto.PetBleActivityDto;
import com.example.RSW.vo.PetBleActivity;
import com.example.RSW.vo.ResultData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

@Controller
public class BleActivityController {
    @Autowired
    private PetBleActivityService bleService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @PostMapping("/usr/pet/activity/save")
    @ResponseBody
    public ResponseEntity<String> saveActivity(@RequestBody PetBleActivityDto dto) {
        bleService.save(dto);

        // 저장된 데이터 다시 조회 (id, enteredAt 등 포함)
        PetBleActivity saved = bleService.findLatestByPetId(dto.getPetId());

        // WebSocket 전송
        messagingTemplate.convertAndSend("/topic/activity/" + dto.getPetId(), saved);

        return ResponseEntity.ok("BLE Activity Saved");
    }


    // ✅ BLE 활동 리스트 조회 (JSP용)
    @GetMapping("/usr/pet/activity")
    @ResponseBody
    public String showBleActivityList(@RequestParam("petId") int petId, Model model) {
        List<PetBleActivity> activities = bleService.getActivitiesByPetId(petId);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // ✅ LocalDateTime 지원 모듈
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ✅ ISO-8601 형식 유지

        String activitiesJson = "[]"; // ✅ 기본값
        try {
            activitiesJson = objectMapper.writeValueAsString(activities);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // JSON 직렬화 오류 확인
        }

        model.addAttribute("activities", activities);           // 테이블용
        model.addAttribute("activitiesJson", activitiesJson);   // 차트용
        model.addAttribute("petId", petId);

        return "usr/pet/activity";
    }
    
    // ✅ BLE 활동 리스트 조회 (JSP용)
    @GetMapping("/usr/petJ/activity")
    @ResponseBody
    public ResultData showJsonActivityList(@RequestParam("petId") int petId, Model model) {
        List<PetBleActivity> activities = bleService.getActivitiesByPetId(petId);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // ✅ LocalDateTime 지원 모듈
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ✅ ISO-8601 형식 유지

        String activitiesJson = "[]"; // ✅ 기본값
        try {
            activitiesJson = objectMapper.writeValueAsString(activities);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // JSON 직렬화 오류 확인
        }
        return ResultData.from("S-1", "테이블용/차트용/펫아이 가져오기", "activities",activities, "activitiesJson", activitiesJson, "petId", petId );
    }

    @GetMapping("/usr/pet/activity/list")
    @ResponseBody
    public List<PetBleActivity> getActivityList(@RequestParam int petId, @RequestParam String date) {
        LocalDate targetDate = LocalDate.parse(date);
        return bleService.getByPetIdAndDate(petId, targetDate);
    }


}
