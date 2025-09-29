package com.example.RSW.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.example.RSW.dto.WeightPoint;
import com.example.RSW.service.PetReportService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetReportController {

    private final PetReportService service;

    @GetMapping("/weight-timeline")
    public Map<String, Object> weightTimeline(@RequestParam long petId){
        List<WeightPoint> points = service.getWeightTimeline(petId);
        Map<String,Object> res = new HashMap<>();
        res.put("petId", petId);
        res.put("points", points);
        return res;
    }
}
