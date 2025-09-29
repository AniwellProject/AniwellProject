package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.example.RSW.vo.District;

@Mapper
public interface DistrictRepository {

	List<String> getDistinctCities();

	List<String> getDistrictsByCity(@Param("city") String city);

	List<String> getDongsByDistrict(@Param("district") String district);

	void insertDistrict(District district); // 단건 삽입

	District findByFullName(@Param("fullName") String fullName); // 주소명으로 조회

	List<String> findDongsByCityAndDistrict(@Param("city") String city, @Param("district") String district);

	// ✅ districtId로 단건 조회 (크루 상세에서 사용)
	District getDistrictById(@Param("id") int id);

	District findByDong(String dong);

	int getDistrictIdByFullAddress(String city, String district, String dong);
}
