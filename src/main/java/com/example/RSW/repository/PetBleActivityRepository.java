package com.example.RSW.repository;

import com.example.RSW.vo.PetBleActivity;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface PetBleActivityRepository {
    void insertActivity(Map<String, Object> param);

    List<PetBleActivity> getActivitiesByPetId(int petId);

    PetBleActivity findLatestByPetId(int petId);

    List<PetBleActivity> getByPetIdAndDate(int petId, LocalDate targetDate);
}
