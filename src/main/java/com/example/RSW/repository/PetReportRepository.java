package com.example.RSW.repository;

import com.example.RSW.dto.WeightPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetReportRepository {
    List<WeightPoint> selectWeightTimeline(@Param("petId") long petId);
}
