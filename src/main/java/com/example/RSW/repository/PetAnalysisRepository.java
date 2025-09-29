package com.example.RSW.repository;

import com.example.RSW.vo.PetAnalysis;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetAnalysisRepository {
    List<PetAnalysis> getAnalysisByPetId(int petId);

    void insertAnalysis(PetAnalysis analysis);
}
