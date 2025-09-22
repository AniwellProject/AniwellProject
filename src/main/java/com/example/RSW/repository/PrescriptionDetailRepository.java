package com.example.RSW.repository;

import com.example.RSW.vo.PrescriptionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrescriptionDetailRepository {

    int insert(PrescriptionDetail row);

    int[] insertBatch(@Param("list") List<PrescriptionDetail> list);

    int update(PrescriptionDetail row);

    void delete(@Param("id") int id);

    PrescriptionDetail selectById(@Param("id") int id);

    List<PrescriptionDetail> selectByDocumentId(@Param("documentId") int documentId);

    // medical_document JOIN visit 로 방문아이디로 조회
    List<PrescriptionDetail> selectByVisitId(@Param("visitId") int visitId);

    // medical_document JOIN visit 로 펫아이디로 조회
    List<PrescriptionDetail> selectByPetId(@Param("petId") int petId);
}
