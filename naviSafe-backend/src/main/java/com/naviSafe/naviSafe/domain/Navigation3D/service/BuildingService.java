package com.naviSafe.naviSafe.domain.Navigation3D.service;

import com.naviSafe.naviSafe.domain.Navigation3D.dto.BuildingGeometry;
import com.naviSafe.naviSafe.domain.Navigation3D.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private static final double BUILDING_RADIUS = 100.0;
    private final BuildingRepository buildingRepository;

    public List<BuildingGeometry> findBuildings(double longitude, double latitude) {
        return buildingRepository.findBuildings(longitude, latitude, BUILDING_RADIUS);
    }
}
