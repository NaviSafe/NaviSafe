package com.naviSafe.naviSafe.domain.Navigation3D.repository;

import com.naviSafe.naviSafe.domain.Navigation3D.dto.BuildingGeometry;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class BuildingRepositoryTest {

    private final BuildingRepository buildingRepository;
    Logger log  = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public BuildingRepositoryTest(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Test
    void findAll() {
        double longitude = 126.96320522724326;
        double latitude = 37.559747577258186;
        double radius = 100.0;

        List<BuildingGeometry> result = buildingRepository.findBuildings(longitude, latitude, radius);

        for(BuildingGeometry building : result) {
            log.info("HEIGHT  : {}", building.getHeight());
            log.info("GEOM    : {}", building.getGeom());
            log.info("---------------------------");
        }

        log.info("total {}", result.size());
    }
}