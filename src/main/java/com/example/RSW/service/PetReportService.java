package com.example.RSW.service;

import java.util.List;

import com.example.RSW.dto.WeightPoint;
import com.example.RSW.repository.PetReportRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class PetReportService {
    private final PetReportRepository mapper;

    public List<WeightPoint> getWeightTimeline(long petId){
        return mapper.selectWeightTimeline(petId);
    }
}
