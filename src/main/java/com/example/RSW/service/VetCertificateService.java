package com.example.RSW.service;

import com.example.RSW.repository.VetCertificateRepository;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.VetCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class VetCertificateService {

    @Autowired
    private VetCertificateRepository vetCertificateRepository;

    // 인증서 등록
    public ResultData registerCertificate(VetCertificate vetCertificate) {
        vetCertificateRepository.insert(vetCertificate);
        return ResultData.from("S-1", "수의사 인증서가 등록되었습니다.");
    }

    // 전체 인증서 조회 (관리자용)
    public List<VetCertificate> getAllCertificates() {
        return vetCertificateRepository.findAll();
    }

    // 특정 회원의 인증서 조회
    public VetCertificate getCertificateByMemberId(int memberId) {
        return vetCertificateRepository.findByMemberId(memberId);
    }

    // 승인 상태 변경 (0: 대기, 1: 승인, 2: 거절)
    public ResultData updateApprovalStatus(int id, int approved) {
        vetCertificateRepository.updateApprovalStatus(id, approved);
        return ResultData.from("S-1", "승인 상태가 변경되었습니다.");
    }

    // 인증서 삭제
    public ResultData deleteCertificate(int id) {
        vetCertificateRepository.deleteById(id);
        return ResultData.from("S-1", "인증서가 삭제되었습니다.");
    }

    public ResultData deleteCertificateWithFile(VetCertificate cert) {
        // 1. 파일 삭제
        String baseDir = "C:/upload/vet_certificates/";
        String fullPath = baseDir + cert.getFilePath();

        File file = new File(fullPath);
        if (file.exists()) {
            file.delete();
        }

        // 2. DB 삭제
        vetCertificateRepository.deleteById(cert.getId());

        return ResultData.from("S-1", "인증서 및 파일이 삭제되었습니다.");
    }

    public void updateApprovalStatusByMemberId(int memberId, int approved) {
        VetCertificate cert = vetCertificateRepository.findByMemberId(memberId);
        if (cert != null) {
            vetCertificateRepository.updateApprovalStatus(cert.getId(), approved);
        }
    }

}
