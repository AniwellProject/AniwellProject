package com.example.RSW.repository;

import com.example.RSW.vo.Visit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.net.InterfaceAddress;
import java.util.List;

@Mapper
public interface VisitRepository {

    int insertVisit(Visit visit);

    int updateVisit(Visit visit);

    void deleteVisit(int id);

    List<Visit> selectVisitsByPetId(int petId);

    Visit selectVisitById(int id);

    int updateHospital(@Param("visitId") int visitId, @Param("hospital") String hospital);
}
