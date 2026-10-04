package com.naviSafe.naviSafe.domain.Navigation3D.controller;

import com.naviSafe.naviSafe.domain.Navigation3D.dto.BuildingRequestDto;
import com.naviSafe.naviSafe.domain.Navigation3D.dto.RoadGeometry;
import com.naviSafe.naviSafe.domain.Navigation3D.service.RoadService;
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
public class RoadController {

    private final RoadService roadService;

    @PostMapping("/roads")
    public ResponseEntity<List<RoadGeometry>> findRoads(
            @RequestBody BuildingRequestDto requestDto
    ) {
        List<RoadGeometry> roads = roadService.findRoads(
                requestDto.getLongitude(),
                requestDto.getLatitude()
        );

        return ResponseEntity.ok(roads);
    }
}