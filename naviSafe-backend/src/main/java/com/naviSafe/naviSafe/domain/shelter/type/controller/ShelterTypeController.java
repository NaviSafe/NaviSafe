package com.naviSafe.naviSafe.domain.shelter.type.controller;

import com.naviSafe.naviSafe.domain.shelter.type.entity.ShelterType;
import com.naviSafe.naviSafe.domain.shelter.type.service.ShelterTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ShelterTypeController {

    private final ShelterTypeService shelterTypeService;

    @Autowired
    public ShelterTypeController(ShelterTypeService shelterTypeService) {
        this.shelterTypeService = shelterTypeService;
    }

    @GetMapping("/api/naviSafe/shelter/{shelterCode}")
    public ShelterType myShelter(@PathVariable int shelterCode) {
        return shelterTypeService.findByShelterCode(shelterCode)
                .orElseGet(() -> ShelterType.builder()
                        .shelterCode(shelterCode)
                        .shelterCodeName("")  // 없는 경우 기본값
                        .shelterGpsList(List.of()) // 빈 리스트 반환
                        .build()
                );
    }
}
