package com.example.RSW.repository;

import com.example.RSW.vo.VetCertificate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VetCertificateRepository {

    void insert(VetCertificate vetCertificate);

    List<VetCertificate> findAll();

    VetCertificate findByMemberId(int memberId);

    void updateApprovalStatus(int id, int approved);

    void deleteById(int id);
}
