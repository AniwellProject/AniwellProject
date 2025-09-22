package com.example.RSW.service;

import com.example.RSW.repository.PetVaccinationRepository;
import com.example.RSW.vo.PetVaccination;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PetVaccinationService {

    @Autowired
    private final PetVaccinationRepository petVaccinationRepository;

    public PetVaccinationService(PetVaccinationRepository petVaccinationRepository) {
        this.petVaccinationRepository = petVaccinationRepository;
    }

    // 펫 ID로 접종 기록 가져오기
    public List<PetVaccination> getVaccinationsByPetId(int petId) {
        return petVaccinationRepository.getVaccinationByPetId(petId);

    }

    // 접종 기록 지우기
    public ResultData deletePetVaccination(int id) {
        petVaccinationRepository.deletePetVaccination(id);
        return ResultData.from("S-1", "접종 정보 삭제 완료");
    }

    // 접종기록 등록
    public ResultData insertPetVaccination(int petId, String vaccineName, String injectionDate) {


        // 새 백신 등록
        petVaccinationRepository.insertVaccination(petId, vaccineName, injectionDate);

        return ResultData.from("S-1", "접종 등록 완료");
    }

    // 접종 정보 수정
    public ResultData updatePetVaccination(int vaccinationId, String vaccineName, String injectionDate) {

        PetVaccination petVaccination = petVaccinationRepository.getVaccinationById(vaccinationId);
        int petId = petVaccination.getPetId();
        petVaccinationRepository.updatePetVaccination(vaccinationId, vaccineName, injectionDate);
        return ResultData.from("S-1", "접종 정보 수정 완료");
    }

    // ID로 접종 기록 가져오기
    public PetVaccination getVaccinationsById(int vaccinationId) {
        return petVaccinationRepository.getVaccinationById(vaccinationId);
    }

    // ID로 접종 기록된 이벤트의 갯수 가져오기
    public int getPetIdById(int vaccinationId) {
        return petVaccinationRepository.getPetIdById(vaccinationId);
    }

    // 비고 없이 접종 기록 수정
    public ResultData updatePetVaccinationWithNotes(int vaccinationId, String vaccineName, String injectionDate, String notes) {
        PetVaccination petVaccination = petVaccinationRepository.getVaccinationById(vaccinationId);
        int petId = petVaccination.getPetId();

        petVaccinationRepository.updatePetVaccinationWithNotes(vaccinationId, vaccineName, injectionDate,notes);
        return ResultData.from("S-1", "접종 정보 수정 완료");
    }

    // 접종 기록 등록(비고 있음)
    public ResultData insertPetVaccinationWithNotes(int petId, String vaccineName, String injectionDate, String notes) {

        // 새 백신 등록
        petVaccinationRepository.insertPetVaccinationWithNotes(petId, vaccineName, injectionDate, notes);

        return ResultData.from("S-1", "접종 등록 완료");
    }

    public List<PetVaccination> getVaccinationsByMonth(int petId, String yearMonth) {
        return petVaccinationRepository.findByPetIdAndMonth(petId, yearMonth);
    }

    public void updateNextDueDates(int petId, String vaccineName) {
        // 1. 최신 접종일 조회
        LocalDate latestInjectionDate = petVaccinationRepository.findLatestInjectionDate(petId, vaccineName);
        if (latestInjectionDate == null) return;

        // 2. 접종 주기 조회
        Integer intervalMonths = petVaccinationRepository.findIntervalMonthsByVaccine(vaccineName);
        if (intervalMonths == null) return;

        // 3. 다음 예정일 계산
        LocalDate nextDueDate = latestInjectionDate.plusMonths(intervalMonths);

        // 4. 가장 최신 날짜의 nextDueDate 설정
        petVaccinationRepository.updateNextDueDateByInjectionDate(petId, vaccineName, latestInjectionDate, nextDueDate);

        // 5. 최신이 아닌 기록은 nextDueDate = NULL 처리
        petVaccinationRepository.invalidateOldNextDueDates(petId, vaccineName, latestInjectionDate);
    }

}