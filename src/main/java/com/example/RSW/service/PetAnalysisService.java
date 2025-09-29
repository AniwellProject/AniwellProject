package com.example.RSW.service;

import com.example.RSW.repository.PetAnalysisRepository;
import com.example.RSW.vo.PetAnalysis;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PetAnalysisService {

    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    // 펫 ID로 등록된 감정분석 데이터 불러오기
    public List<PetAnalysis> getAnalysisByPetId(int petId) {
        return petAnalysisRepository.getAnalysisByPetId(petId);
    }

    // 감정 분석 데이터 저장
    public void save(PetAnalysis analysis) {
        petAnalysisRepository.insertAnalysis(analysis);
    }
}
