package com.example.RSW.service;

import com.example.RSW.repository.VisitRepository;
import com.example.RSW.vo.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitService {

	@Autowired
	private VisitRepository visitRepository;

	public int insertVisit(Visit visit) {
		visitRepository.insertVisit(visit);
		return visit.getId();
	}

	public int updateVisit(Visit visit) {
		return visitRepository.updateVisit(visit);
	}

	public void deleteVisit(int id) {
		visitRepository.deleteVisit(id);
	}

	public List<Visit> selectVisitsByPetId(int petId) {
		return visitRepository.selectVisitsByPetId(petId);
	}

	public int updateHospital(int visitId, String haspital) {
		return visitRepository.updateHospital(visitId, haspital);
	}
}