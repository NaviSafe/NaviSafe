package com.naviSafe.naviSafe.domain.Navigation3D.service;

import com.naviSafe.naviSafe.domain.Navigation3D.dto.RoadGeometry;
import com.naviSafe.naviSafe.domain.Navigation3D.repository.RoadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadService {

    private static final double ROAD_RADIUS = 100.0;

    private final RoadRepository roadRepository;

    public List<RoadGeometry> findRoads(
            double longitude,
            double latitude
    ) {
        return roadRepository.findRoads(
                longitude,
                latitude,
                ROAD_RADIUS
        );
    }
}
