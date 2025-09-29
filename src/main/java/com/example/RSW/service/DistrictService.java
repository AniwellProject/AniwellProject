package com.example.RSW.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.repository.WalkCrewRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.WalkCrew;

@Service
public class DistrictService {

	private final DistrictRepository districtRepository;

	@Autowired
	public DistrictService(DistrictRepository districtRepository) {
		this.districtRepository = districtRepository;
	}

	public List<String> getCities() {
		return districtRepository.getDistinctCities();
	}

	public List<String> getDistricts(String city) {
		return districtRepository.getDistrictsByCity(city);
	}

	public List<String> getDongs(String district) {
		return districtRepository.getDongsByDistrict(district);
	}

	// districtId로 District 하나 조회
	public District findById(int id) {
		return districtRepository.getDistrictById(id);
	}

	// CSV 삽입용 메서드
	public void insertFromCsv(String filePath) {
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			boolean firstLine = true;

			while ((line = br.readLine()) != null) {
				if (firstLine) {
					firstLine = false;
					continue;
				}

				String[] tokens = line.split(",", -1); // ✅ 공백 필드 유지
				System.out.println("📌 라인 파싱됨: " + Arrays.toString(tokens));

				if (tokens.length < 5) {
					System.out.println("⛔ 필드 부족으로 제외됨: " + Arrays.toString(tokens));
					continue;
				}

				String code = tokens[0].trim();
				String sido = tokens[1].trim();
				String sigungu = tokens[2].trim();
				String dong = tokens[3].trim();
				String ri = tokens[4].trim();

				// ✅ null 또는 공백 필드 검사
				if (sido.isEmpty() || sigungu.isEmpty() || dong.isEmpty()) {
					System.out.println("❗ 필수 필드 누락 → 저장 생략: " + Arrays.toString(tokens));
					continue;
				}

				String fullName = sido + " " + sigungu + " " + dong;
				if (!ri.isEmpty()) {
					fullName += " " + ri;
				}

				System.out.println("✅ 삽입 대상: " + fullName);

				District district = new District();
				district.setCode(code);
				district.setSido(sido);
				district.setSigungu(sigungu);
				district.setDong(dong);
				district.setFullName(fullName);

				districtRepository.insertDistrict(district);
			}

			System.out.println("✅ CSV 데이터 삽입 완료");

		} catch (IOException e) {
			System.err.println("❌ CSV 읽기 실패: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public List<String> findDongsByCityAndDistrict(String city, String district) {
		return districtRepository.findDongsByCityAndDistrict(city, district);
	}

	public District findByDong(String dong) {
		return districtRepository.findByDong(dong);
	}

}