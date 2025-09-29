package com.example.RSW.service;

import com.example.RSW.repository.PetBleActivityRepository;
import com.example.RSW.dto.PetBleActivityDto;
import com.example.RSW.vo.PetBleActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetBleActivityService {

    @Autowired
    private final PetBleActivityRepository bleRepo;

    public PetBleActivityService(PetBleActivityRepository bleRepo) {
        this.bleRepo = bleRepo;
    }

    public void save(PetBleActivityDto dto) {
        Map<String, Object> param = new HashMap<>();
        param.put("petId", dto.getPetId());
        param.put("zoneName", dto.getZoneName());
        param.put("enteredAt", dto.getEnteredAt());
        param.put("exitedAt", dto.getExitedAt());
        param.put("durationSec", dto.getDurationSec());
        param.put("rssi", dto.getRssi());

        bleRepo.insertActivity(param);
    }

    public List<PetBleActivity> getActivitiesByPetId(int petId) {
        return bleRepo.getActivitiesByPetId(petId);
    }

    public PetBleActivity findLatestByPetId(int petId) {
        return bleRepo.findLatestByPetId(petId);
    }

    public List<PetBleActivity> getByPetIdAndDate(int petId, LocalDate targetDate) {
         return bleRepo.getByPetIdAndDate(petId,targetDate);
    }
}
