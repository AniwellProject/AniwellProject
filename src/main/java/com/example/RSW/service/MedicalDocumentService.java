package com.example.RSW.service;

import com.example.RSW.dto.LabDocumentDto;
import com.example.RSW.repository.MedicalDocumentRepository;
import com.example.RSW.vo.MedicalDocument;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MedicalDocumentService {

	@Autowired
	private MedicalDocumentRepository medicalDocumentRepository;

	public int insertDocument(MedicalDocument doc) {
			int rows = medicalDocumentRepository.insertDocument(doc); // rows 무시
			return doc.getId();
	}

	public int updateDocument(MedicalDocument doc) {
		return medicalDocumentRepository.updateDocument(doc);
	}

	public void deleteDocument(@Param("id") int id) {
		medicalDocumentRepository.deleteDocument(id);
	}

	public MedicalDocument selectById(@Param("id") int id) {
		return medicalDocumentRepository.selectById(id);
	}

	public List<MedicalDocument> selectByVisitId(@Param("visitId") int visitId) {
		return medicalDocumentRepository.selectByVisitId(visitId);
	}

	// visit JOIN 해서 petId로 조회
	public List<MedicalDocument> selectByPetId(@Param("petId") int petId) {
		return medicalDocumentRepository.selectByPetId(petId);
	}

	public MedicalDocument findById(int id) {
		return medicalDocumentRepository.findById(id);
	}

	public MedicalDocument findLatestByVisitId(int visitId) {
		return medicalDocumentRepository.findLatestByVisitId(visitId);
	}

	public Map<String,Object> getLabDocsByPetId(int petId, int page, int pageSize){
		int limit = Math.max(1, pageSize);
		int offset = Math.max(0, page-1) * limit;

		List<LabDocumentDto> rows = medicalDocumentRepository.selectLabDocsByPetId(petId, offset, limit);
		int total = medicalDocumentRepository.countLabDocsByPetId(petId);

		Map<String,Object> out = new HashMap<>();
		out.put("rows", rows);
		out.put("total", total);
		out.put("page", page);
		out.put("pageSize", pageSize);
		return out;
	}
}