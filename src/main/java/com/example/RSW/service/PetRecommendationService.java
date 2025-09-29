package com.example.RSW.service;

import com.example.RSW.repository.PetRecommendationRepository;
import com.example.RSW.vo.PetRecommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetRecommendationService {

    @Autowired
    private PetRecommendationRepository recommendationRepository;

    // 즐겨찾기 장소 가져오기
    public List<PetRecommendation> getFavoriteNamesByMember(int memberId) {
        return recommendationRepository.selectPlaceNamesByMemberId(memberId);
    }

    // 타입이 있어?
    public boolean isFavorited(int memberId, String name) {
        return recommendationRepository.countByMemberAndName(memberId, name) > 0;
    }

    // 즐겨찾기 저장
    public void saveFavorite(int memberId, String type, String name, String address, String phone, String mapUrl) {
        recommendationRepository.insert(memberId, type, name, address, phone, mapUrl);
    }

    // 즐겨찾기 삭제
    public void removeFavorite(int memberId, String name) {
        recommendationRepository.deleteByMemberAndName(memberId, name);
    }

    // 즐겨찾기 타입 이름만 가져옴
    public List<String> getFavoriteNamesOnly(int memberId) {
        return recommendationRepository.getFavoriteNamesOnly(memberId);
    }
}
