package com.example.RSW.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.service.*;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

@Controller
public class petJsonDataController {

    @Autowired
    Rq rq;

    @Autowired
    private PetService petService;

    @Autowired
    private WalkCrewService walkCrewService;

    @Autowired
    private PetVaccinationService petVaccinationService;

    @Autowired
    private PetAnalysisService petAnalysisService;

    @Autowired
    private CalendarEventService calendarEventService;

    @Autowired
    private PetRecommendationService petRecommendationService;

    @Autowired
    private Cloudinary cloudinary;


    // 펫 상세보기
    @GetMapping("/api/pet/detail")
    public ResponseEntity<?> getPetDetail(@RequestParam("petId") int petId) {
        Member loginedMember = rq.getLoginedMember();
        Pet pet = petService.getPetsById(petId);

        if (pet.getMemberId() != loginedMember.getId()) {
            return ResponseEntity.status(403).body(Map.of("resultCode", "F-1", "msg", "권한이 없습니다."));
        }

        List<PetVaccination> list = petVaccinationService.getVaccinationsByPetId(petId);
        List<Map<String, Object>> events = new ArrayList<>();

        for (PetVaccination pv : list) {
            // 접종 이벤트
            Map<String, Object> injEvent = new HashMap<>();
            injEvent.put("id", pv.getId());
            injEvent.put("title", pv.getVaccineName() + " 접종");
            injEvent.put("start", pv.getInjectionDate().toString());
            injEvent.put("color", "#4caf50");

            events.add(injEvent);

            // 다음 예정 이벤트
            if (pv.getNextDueDate() != null) {
                Map<String, Object> nextEvent = new HashMap<>();
                nextEvent.put("id", pv.getId());
                nextEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 다음 예정");
                nextEvent.put("start", pv.getNextDueDate().toString());
                nextEvent.put("color", "#f44336");

                events.add(nextEvent);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("resultCode", "S-1");
        response.put("msg", "성공");
        response.put("member", loginedMember);
        response.put("pet", pet);
        response.put("events", events);

        return ResponseEntity.ok(response);
    }

    // 펫 목록 / 크루목록 정보 불러오기
    @GetMapping("/api/pet/list")
    public ResponseEntity<?> getPetList(@RequestParam("memberId") int memberId) {
        int loginId = rq.getLoginedMemberId();
        if (loginId != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        List<Pet> pets = petService.getPetsByMemberId(memberId);
        List<WalkCrew> crews = walkCrewService.getWalkCrews(memberId);
        Member loginedMember = rq.getLoginedMember();
        Map<String, Object> result = new HashMap<>();
        result.put("resultCode", "S-1");
        result.put("msg", "펫 및 크루 목록 조회 성공");
        result.put("member", loginedMember); // 로그인 멤버
        result.put("pets", pets); // 로그인 멤버의 해당 펫
        result.put("crews", crews); // 로그인 멤버의 가입 크루 목록

        return ResponseEntity.ok(result);
    }

    // 펫 등록하기
    @PostMapping(value = "/api/pet/join", consumes = "multipart/form-data")
    public ResponseEntity<?> apiDoJoin(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam String name,
            @RequestParam String species,
            @RequestParam String breed,
            @RequestParam String gender,
            @RequestParam String birthDate,
            @RequestParam double weight) {

        // 유효성 검사
        if (Ut.isEmptyOrNull(name)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-1", "msg", "이름을 입력하세요"));
        if (Ut.isEmptyOrNull(species)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-2", "msg", "종을 입력하세요"));
        if (Ut.isEmptyOrNull(breed)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-3", "msg", "품종을 입력하세요"));
        if (Ut.isEmptyOrNull(gender)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-4", "msg", "성별을 입력하세요"));
        if (Ut.isEmptyOrNull(birthDate)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-5", "msg", "생일을 입력하세요"));
        if (weight <= 0) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-6", "msg", "몸무게를 입력하세요"));

        String imagePath = null;
        if (!photo.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
                imagePath = (String) uploadResult.get("secure_url");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(Map.of("resultCode", "F-7", "msg", "사진 업로드 중 오류 발생"));
            }
        }

        ResultData joinRd = petService.insertPet(
                rq.getLoginedMemberId(),
                name, species, breed, gender, birthDate, weight, imagePath
        );

        return ResponseEntity.ok(Map.of(
                "resultCode", joinRd.getResultCode(),
                "msg", joinRd.getMsg()
        ));
    }

    // 펫 정보 수정하기 위해ㅔ 기존 정보 불러오기
    @GetMapping("/api/pet/modify")
    public ResponseEntity<?> getPetForModify(@RequestParam("petId") int petId) {
        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);

        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "resultCode", "S-1",
                "msg", "펫 정보 조회 성공",
                "pet", pet // 해당 펫
        ));
    }

    // 펫 정보 수정하기
    @PostMapping(value = "/api/pet/modify", consumes = "multipart/form-data")
    public ResponseEntity<?> apiModifyPet(@RequestParam("petId") int petId,
                                          @RequestParam String name,
                                          @RequestParam String species,
                                          @RequestParam String breed,
                                          @RequestParam String gender,
                                          @RequestParam String birthDate,
                                          @RequestParam double weight,
                                          @RequestParam(value = "photo", required = false) MultipartFile photo) {

        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);
        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of("resultCode", "F-0", "msg", "권한이 없습니다."));
        }

        // 유효성 검사
        if (Ut.isEmptyOrNull(name)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-1", "msg", "이름을 입력하세요"));
        if (Ut.isEmptyOrNull(species)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-2", "msg", "종을 입력하세요"));
        if (Ut.isEmptyOrNull(breed)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-3", "msg", "품종을 입력하세요"));
        if (Ut.isEmptyOrNull(gender)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-4", "msg", "성별을 입력하세요"));
        if (Ut.isEmptyOrNull(birthDate)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-5", "msg", "생일을 입력하세요"));
        if (weight <= 0) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-6", "msg", "몸무게를 입력하세요"));

        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
                photoPath = (String) uploadResult.get("secure_url");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(Map.of("resultCode", "F-7", "msg", "사진 업로드 실패"));
            }
        }

        ResultData modifyRd;
        if (photoPath == null) {
            modifyRd = petService.updatePetyWithoutPhoto(petId, name, species, breed, gender, birthDate, weight);
        } else {
            modifyRd = petService.updatePet(petId, name, species, breed, gender, birthDate, weight, photoPath);
        }

        return ResponseEntity.ok(Map.of(
                "resultCode", modifyRd.getResultCode(),
                "msg", modifyRd.getMsg()
        ));
    }

    // 펫 감정 갤러리 정보 불러오기
    @GetMapping("/api/pet/gallery")
    public ResponseEntity<?> getPetGallery(@RequestParam("petId") int petId) {
        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);

        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        List<PetAnalysis> analysisList = petAnalysisService.getAnalysisByPetId(petId);

        return ResponseEntity.ok(Map.of(
                "resultCode", "S-1",
                "msg", "감정 분석 결과 조회 성공",
                "analysisList", analysisList // 감정  분석 결과 리스트
        ));
    }

    // 펫 감정 분석하기
    @PostMapping("/api/pet/analysis/do")
    @ResponseBody
    public Map<String, Object> doAnalysis(
            @RequestParam("petId") int petId,
            @RequestParam("species") String species,
            @RequestParam("imageFile") MultipartFile imageFile) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 1. Cloudinary 업로드
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");

            // 2. 임시 파일로 저장해서 파이썬에 전달
            File tempFile = File.createTempFile("emotion_", ".jpg");
            imageFile.transferTo(tempFile);

            // 3. 종에 따라 파이썬 파일 선택
            String scriptPath;
            if ("강아지".equals(species)) {
                scriptPath = "/Users/e-suul/Desktop/ESeul-main/dog_test.py";
            } else {
                scriptPath = "/Users/e-suul/Desktop/ESeul-main/cat_test.py";
            }

            // 4. 파이썬 실행
            String command = "python3 " + scriptPath + " " + tempFile.getAbsolutePath();
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line;
            String lastLine = null;
            while ((line = reader.readLine()) != null) {
                System.out.println("🐍 Python output: " + line);
                lastLine = line;
            }

            process.waitFor();
            System.out.println("✅ 파이썬 종료 코드: " + process.exitValue());
            System.out.println("⚠ 최종 파이썬 결과 문자열: " + lastLine);

            if (lastLine == null || !lastLine.trim().startsWith("{")) {
                throw new RuntimeException("❌ 파이썬 실행 실패 또는 JSON 형식 아님");
            }

            // 5. JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(lastLine);
            String emotion = root.get("emotion").asText();
            double confidence = root.get("probabilities").get(emotion).asDouble();

            // 6. DB 저장
            PetAnalysis analysis = new PetAnalysis();
            analysis.setPetId(petId); // 펫 Id
            analysis.setImagePath(imageUrl); // Cloudinary URL 저장
            analysis.setEmotionResult(emotion); // 펫의 최종 감정
            analysis.setConfidence(confidence); // 최종 감정  %
            petAnalysisService.save(analysis); //감정 DB 저장

            // 7. 응답 반환
            result.put("emotionResult", emotion); // 감정 결과
            result.put("confidence", String.format("%.2f", confidence)); // 감정 %
            result.put("imagePath", imageUrl); // 이미지

            Map<String, Double> probabilities = new HashMap<>();
            root.get("probabilities").fields().forEachRemaining(entry -> {
                probabilities.put(entry.getKey(), entry.getValue().asDouble());
            });
            result.put("probabilities", probabilities);

            // 임시 파일 삭제
            tempFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
            result.put("emotionResult", "error");
            result.put("confidence", "0");
            result.put("imagePath", "");
        }

        return result;
    }
    // 펫 삭제하기
    @DeleteMapping("/api/pet/delete")
    public ResponseEntity<?> apiDeletePet(@RequestParam("petId") int petId) {
        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);

        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        ResultData deleteRd = petService.deletePet(petId);

        return ResponseEntity.ok(Map.of(
                "resultCode", deleteRd.getResultCode(),
                "msg", deleteRd.getMsg()
        ));
    }

    // 백신 접종기럭 등록하기 위해 필요한 정보 불러오기
    @GetMapping("/api/pet/vaccination/registration")
    public ResponseEntity<?> getVaccinationRegistrationInfo(@RequestParam("petId") int petId) {
        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);

        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "resultCode", "S-1",
                "msg", "백신 등록 준비 완료",
                "pet", pet // 해당 펫
        ));
    }

    // 백신 접종 기록 등록하기
    @PostMapping("/api/pet/vaccination/register")
    public ResponseEntity<?> apiRegisterVaccination(@RequestBody Map<String, Object> body) {
        int petId = (int) body.get("petId");
        String vaccineName = (String) body.get("vaccineName");
        String injectionDate = (String) body.get("injectionDate");
        String notes = (String) body.getOrDefault("notes", null);

        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);
        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-0",
                    "msg", "권한이 없습니다."
            ));
        }

        if (Ut.isEmptyOrNull(vaccineName)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "resultCode", "F-2",
                    "msg", "백신 이름을 입력하세요"
            ));
        }

        if (Ut.isEmptyOrNull(injectionDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "resultCode", "F-3",
                    "msg", "접종 날짜를 입력하세요"
            ));
        }

        ResultData result;
        if (notes == null || notes.trim().isEmpty()) {
            result = petVaccinationService.insertPetVaccination(petId, vaccineName, injectionDate);
        } else {
            result = petVaccinationService.insertPetVaccinationWithNotes(petId, vaccineName, injectionDate, notes);
        }

        return ResponseEntity.ok(Map.of(
                "resultCode", result.getResultCode(),
                "msg", result.getMsg()
        ));
    }

    // 해당 백신 접종 수정하기 위해 기록 불러오기
    @GetMapping("/api/pet/vaccination/modify")
    public ResponseEntity<?> getVaccinationForModify(@RequestParam("vaccinationId") int vaccinationId) {
        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        if (petVaccination == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "resultCode", "F-0",
                    "msg", "해당 백신 정보를 찾을 수 없습니다."
            ));
        }

        int petId = petVaccination.getPetId();
        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);

        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "resultCode", "S-1",
                "msg", "백신 정보 조회 성공",
                "petVaccination", petVaccination // 해당 펫이 맞은 백신
        ));
    }

    // 백신 접종 기록 수정하기
    @PutMapping("/api/pet/vaccination/modify")
    public ResponseEntity<?> apiVaccinationModify(@RequestBody Map<String, Object> body) {
        int vaccinationId = (int) body.get("vaccinationId");
        String vaccineName = (String) body.get("vaccineName");
        String injectionDate = (String) body.get("injectionDate");
        String notes = (String) body.getOrDefault("notes", "");

        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        if (petVaccination == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "resultCode", "F-0",
                    "msg", "해당 백신 정보를 찾을 수 없습니다."
            ));
        }

        int petId = petVaccination.getPetId();
        Pet pet = petService.getPetsById(petId);
        int memberId = rq.getLoginedMemberId();
        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        if (Ut.isEmptyOrNull(vaccineName)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "resultCode", "F-2",
                    "msg", "백신명을 입력하세요"
            ));
        }

        if (Ut.isEmptyOrNull(injectionDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "resultCode", "F-3",
                    "msg", "접종일자를 입력하세요"
            ));
        }

        ResultData modifyRd;
        if (Ut.isEmptyOrNull(notes)) {
            modifyRd = petVaccinationService.updatePetVaccination(vaccinationId, vaccineName, injectionDate);
        } else {
            modifyRd = petVaccinationService.updatePetVaccinationWithNotes(vaccinationId, vaccineName, injectionDate, notes);
        }

        return ResponseEntity.ok(Map.of(
                "resultCode", modifyRd.getResultCode(),
                "msg", modifyRd.getMsg()
        ));
    }

    // 백신 접종 상세보기
    @GetMapping("/api/pet/vaccination/detail")
    public ResponseEntity<?> getVaccinationDetail(@RequestParam("vaccinationId") int vaccinationId) {
        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        if (petVaccination == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "resultCode", "F-0",
                    "msg", "백신 정보를 찾을 수 없습니다."
            ));
        }

        int petId = petVaccination.getPetId();
        Pet pet = petService.getPetsById(petId);
        int memberId = rq.getLoginedMemberId();

        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "resultCode", "S-1",
                "msg", "백신 정보 조회 성공",
                "petVaccination", petVaccination //해당 백신 정보
        ));
    }

    // 백신 접종 이벤트 삭제
    @DeleteMapping("/api/pet/vaccination/delete")
    public ResponseEntity<?> apiVaccinationDelete(@RequestParam("vaccinationId") int vaccinationId) {
        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        if (petVaccination == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "resultCode", "F-0",
                    "msg", "백신 정보를 찾을 수 없습니다."
            ));
        }

        int petId = petVaccination.getPetId();
        Pet pet = petService.getPetsById(petId);
        int memberId = rq.getLoginedMemberId();

        if (pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        petVaccinationService.deletePetVaccination(vaccinationId);

        return ResponseEntity.ok(Map.of(
                "resultCode", "S-1",
                "msg", "삭제가 완료되었습니다.",
                "petId", petId // 백신을 맞은 해당 펫
        ));
    }

    // 감정일기 리스트 불러오기
    @GetMapping("/api/pet/daily")
    public ResponseEntity<?> getPetEmotionDiary(@RequestParam("petId") int petId) {
        int memberId = rq.getLoginedMemberId();
        Pet pet = petService.getPetsById(petId);

        if (pet == null || pet.getMemberId() != memberId) {
            return ResponseEntity.status(403).body(Map.of(
                    "resultCode", "F-1",
                    "msg", "권한이 없습니다."
            ));
        }

        List<CalendarEvent> events = calendarEventService.getEventsByPetId(petId);

        return ResponseEntity.ok(Map.of(
                "resultCode", "S-1",
                "msg", "감정일기 조회 성공",
                "petId", petId, // 해당 펫 Id
                "events", events // 해당 펫이 작성한 감정일기
        ));
    }

    // 감정일기 추가하기
    @PostMapping("/api/pet/daily")
    public ResponseEntity<?> addDiary(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String content = body.get("content");
        String eventDateStr = body.get("eventDate");
        int petId = Integer.parseInt(body.get("petId"));

        if (Ut.isEmptyOrNull(title)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-1", "msg", "감정을 선택하세요"));
        if (Ut.isEmptyOrNull(content)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-2", "msg", "내용을 입력하세요"));
        if (Ut.isEmptyOrNull(eventDateStr)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-3", "msg", "날짜를 선택하세요"));

        LocalDate eventDate = LocalDate.parse(eventDateStr);
        Pet pet = petService.getPetsById(petId);
        if (pet == null) return ResponseEntity.status(404).body(Map.of("resultCode", "F-4", "msg", "해당 반려동물을 찾을 수 없습니다."));

        ResultData result = calendarEventService.insert(pet.getMemberId(), eventDate, title, petId, content);
        return ResponseEntity.ok(Map.of("resultCode", result.getResultCode(), "msg", result.getMsg()));
    }

    // 감정일기 수정하기
    @PutMapping("/api/pet/daily")
    public ResponseEntity<?> updateDiary(@RequestBody Map<String, String> body) {
        int id = Integer.parseInt(body.get("id"));
        String title = body.get("title");
        String content = body.get("content");
        String eventDateStr = body.get("eventDate");

        if (Ut.isEmptyOrNull(title)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-1", "msg", "감정을 선택하세요"));
        if (Ut.isEmptyOrNull(content)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-2", "msg", "내용을 입력하세요"));
        if (Ut.isEmptyOrNull(eventDateStr)) return ResponseEntity.badRequest().body(Map.of("resultCode", "F-3", "msg", "날짜를 선택하세요"));

        LocalDate eventDate = LocalDate.parse(eventDateStr);
        ResultData result = calendarEventService.update(id, eventDate, title, content);
        return ResponseEntity.ok(Map.of("resultCode", result.getResultCode(), "msg", result.getMsg()));
    }

    //감정일기 삭제하기
    @DeleteMapping("/api/pet/daily/{id}")
    public ResponseEntity<?> deleteDiary(@PathVariable("id") int id) {
        CalendarEvent event = calendarEventService.getEventsById(id);
        if (event == null) return ResponseEntity.status(404).body(Map.of("resultCode", "F-1", "msg", "해당 일기를 찾을 수 없습니다."));

        calendarEventService.delete(id);
        return ResponseEntity.ok(Map.of("resultCode", "S-1", "msg", "삭제 완료", "petId", event.getPetId()));
    }

    //감정일기 상세보기
    @GetMapping("/api/pet/daily/{id}")
    public ResponseEntity<?> getDiaryDetail(@PathVariable("id") int id) {
        CalendarEvent event = calendarEventService.getEventsById(id);
        if (event == null) return ResponseEntity.status(404).body(Map.of("resultCode", "F-1", "msg", "해당 일기를 찾을 수 없습니다."));
        return ResponseEntity.ok(Map.of("resultCode", "S-1", "calendarEvent", event));
    }


}
