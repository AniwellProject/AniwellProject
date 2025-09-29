package com.example.RSW.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.ApiRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.repository.WalkCrewRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.WalkCrew;

@Service
public class ApiService {

	private final ApiRepository apiRepository;

	public ApiService(ApiRepository apiRepository) {
		this.apiRepository = apiRepository;
	}

	public List<String> getDongListByDistrict(String district) {
		return apiRepository.findDongListByDistrict(district);
	}

	public List<String> getCityList() {
		return apiRepository.findDistinctCities();
	}

	public List<String> getDistrictListByCity(String city) {
		return apiRepository.findDistrictListByCity(city);
	}

}