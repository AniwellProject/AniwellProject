package com.example.RSW.service;

import com.example.RSW.repository.PrescriptionDetailRepository;
import com.example.RSW.vo.PrescriptionDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionDetailService {

    @Autowired
    private PrescriptionDetailRepository prescriptionDetailRepository;

    public int insert(PrescriptionDetail row){
        return prescriptionDetailRepository.insert(row);
    }

    public int[] insertBatch(@Param("list") List<PrescriptionDetail> list){
        return prescriptionDetailRepository.insertBatch(list);
    }

    public int update(PrescriptionDetail row){
        return prescriptionDetailRepository.update(row);
    }

    public void delete(@Param("id") int id){
        prescriptionDetailRepository.delete(id);
    }

    public PrescriptionDetail selectById(@Param("id") int id){
        return prescriptionDetailRepository.selectById(id);
    }

    public List<PrescriptionDetail> selectByDocumentId(@Param("documentId") int documentId){
        return prescriptionDetailRepository.selectByDocumentId(documentId);
    }

    // medical_document JOIN visit 로 방문아이디로 조회
    public List<PrescriptionDetail> selectByVisitId(@Param("visitId") int visitId){
        return prescriptionDetailRepository.selectByVisitId(visitId);
    }

    // medical_document JOIN visit 로 펫아이디로 조회
    public List<PrescriptionDetail> selectByPetId(@Param("petId") int petId){
        return prescriptionDetailRepository.selectByPetId(petId);
    }
}
