package com.naviSafe.naviSafe.domain.Navigation3D.controller;

import com.naviSafe.naviSafe.domain.Navigation3D.dto.BuildingGeometry;
import com.naviSafe.naviSafe.domain.Navigation3D.dto.BuildingRequestDto;
import com.naviSafe.naviSafe.domain.Navigation3D.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/naviSafe/navigation3D")
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping("/buildings")
    public ResponseEntity<List<BuildingGeometry>> findBuildings(@RequestBody BuildingRequestDto requestDto) {
        List<BuildingGeometry> buildings = buildingService.findBuildings(
                requestDto.getLongitude(),
                requestDto.getLatitude()
        );

        return ResponseEntity.ok(buildings);
    }
}
