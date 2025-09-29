package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.WalkCrew;

@Mapper
public interface ApiRepository {

	// 특정 구(district)에 해당하는 동 리스트
	List<String> findDongListByDistrict(@Param("district") String district);

	// 전체 시(city) 리스트 (중복 제거)
	List<String> findDistinctCities();

	// 특정 시(city)에 해당하는 구 리스트
	List<String> findDistrictListByCity(@Param("city") String city);

}