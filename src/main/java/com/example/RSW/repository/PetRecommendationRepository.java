package com.example.RSW.repository;

import com.example.RSW.vo.PetRecommendation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetRecommendationRepository {

    List<PetRecommendation> selectPlaceNamesByMemberId(int memberId);

    int countByMemberAndName(int memberId, String name);

    void insert(int memberId, String type, String name, String address, String phone, String mapUrl);

    void deleteByMemberAndName(int memberId, String name);

    List<String> getFavoriteNamesOnly(int memberId);
}
