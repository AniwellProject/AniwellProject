package com.example.RSW.repository;

import com.example.RSW.vo.Pet;
import com.example.RSW.vo.PetFeedLog;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PetRepository {
	List<Pet> getPetsByMemberId(int memberId); // 회원 ID로 펫 목록 조회

	// VO 기반 insert
	void insertPet(Pet pet);

	// VO 기반 update
	void updatePet(Pet pet);

	void deletePet(int id);

	void insertPet(int memberId, String name, String species, String breed, String gender, String birthDate,
				   double weight, String photo);

	int getLastInsertId();

	void updatePetWithoutPhoto(int petId, String name, String species, String breed, String gender, String birthDate,
							   double weight);

	void updatePet(int petId, String name, String species, String breed, String gender, String birthDate, double weight,
				   String photo);

	Pet getPetsById(int petId);

	List<Pet> findPetsWithBirthdayInDays(List<Integer> integers);

	// [추가] 현재 저장된 펫의 weight 조회
	Double getPetWeightById(@Param("petId") int petId);

	// [추가] 해당 펫의 체중 로그 존재 여부(0이면 로그 없음)
	int countWeightLogsByPetId(@Param("petId") int petId);

	// ✅ [추가] 가장 최근 체중 1건(없으면 null)
	Double findLastWeightByPetId(@Param("petId") int petId);

	// ✅ [추가] 체중 로그 INSERT (pet_weight_log)
	int insertWeightLog(@Param("petId") int petId, @Param("weightKg") double weightKg, @Param("source") String source,
						@Param("note") String note);

	// ✅ [추가] 펫 현재 체중 업데이트 (weightUpdatedAt 컬럼이 없다면 XML에서 해당 컬럼 줄은 주석 처리)
	int updatePetWeight(@Param("petId") int petId, @Param("weightKg") double weightKg);

	// ✅ [추가] 방금 등록한 펫(동명이인 대비: memberId+name) 중 가장 최근 PK
	Integer findNewestPetIdByMemberAndName(@Param("memberId") int memberId, @Param("name") String name);

	// 진행중 기본 사료 1건 조회 (isPrimary=1 AND endedAt IS NULL)
	Map<String, Object> findActivePrimaryFood(@Param("petId") int petId); // {brand, feedType}

	// 진행중 기본 사료 종료 (endedAt = CURRENT_DATE())
	int closeActivePrimaryFood(@Param("petId") int petId);

	// 새 기본 사료 시작 (startedAt = CURRENT_DATE(), endedAt = NULL)
	int insertPrimaryFood(@Param("petId") int petId,
						  @Param("brand") String brand,
						  @Param("feedType") String feedType,
						  @Param("productName") String productName, // ★
						  @Param("flavor") String flavor);          // ★


}
