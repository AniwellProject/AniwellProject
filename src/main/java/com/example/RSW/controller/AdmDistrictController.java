package com.example.RSW.controller;

import com.example.RSW.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/adm/district")
public class AdmDistrictController {

	private final DistrictService districtService;

	@Autowired
	public AdmDistrictController(DistrictService districtService) {
		this.districtService = districtService;
	}

	// CSV 경로 입력해서 등록 (ex:
	// http://localhost:8080/adm/district/insert?path=C:/data/districts.csv)
	@GetMapping("/insert")
	@ResponseBody
	public String insertDistrictsFromCsv(@RequestParam String path) {
		districtService.insertFromCsv(path);
		return "✅ 행정동 CSV 삽입 완료 (경로: " + path + ")";
	}

}
