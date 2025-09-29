package com.example.RSW.repository;

import com.example.RSW.vo.LabResultDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LabResultDetailRepository {
    int insert(LabResultDetail row);

    int insertBatch(@Param("list") List<LabResultDetail> list);

    int update(LabResultDetail row);

    void delete(@Param("id") int id);

    LabResultDetail selectById(@Param("id") int id);

    List<LabResultDetail> selectByDocumentId(@Param("documentId") int documentId);

    // medical_document JOIN 으로 방문ID 기준
    List<LabResultDetail> selectByVisitId(@Param("visitId") int visitId);

    // visit JOIN 으로 펫ID 기준
    List<LabResultDetail> selectByPetId(@Param("petId") int petId);

    void deleteByDocumentId(int documentId);
}
