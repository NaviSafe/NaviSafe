package com.naviSafe.naviSafe.domain.roadStatus.repository;

import com.naviSafe.naviSafe.domain.regionCode.entitiy.RegionCode;
import com.naviSafe.naviSafe.domain.regionCode.repository.RegionCodeRepository;
import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatus;
import com.naviSafe.naviSafe.domain.roadTraffic.entitiy.RoadTraffic;
import com.naviSafe.naviSafe.domain.roadTraffic.repository.RoadTrafficRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoadStatusRepositoryTest {
    private final RoadStatusRepository roadStatusRepository;
    private final RoadTrafficRepository roadTrafficRepository;
    private final RegionCodeRepository regionCodeRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    public RoadStatusRepositoryTest(RoadStatusRepository roadStatusRepository, RoadTrafficRepository roadTrafficRepository, RegionCodeRepository regionCodeRepository) {
        this.roadStatusRepository = roadStatusRepository;
        this.roadTrafficRepository = roadTrafficRepository;
        this.regionCodeRepository = regionCodeRepository;
    }

    @Test
    @Transactional
    @DisplayName("링크정보 조회")
    public void testLinkLazy () throws Exception {
        //given
        RoadTraffic roadTraffic1 = roadTrafficRepository.save(
                RoadTraffic
                        .builder()
                        .linkId("1")
                        .prcsSpd(10)
                        .prcsTrvTime(2)
                        .build());
        RegionCode regionCode1 = regionCodeRepository.save(
                RegionCode
                        .builder()
                        .regCode(1)
                        .regName("울산남구")
                        .build());
        roadStatusRepository.save(
                RoadStatus
                        .builder()
                        .linkId("1")
                        .roadName("돋질로")
                        .startNodeNm("시청앞")
                        .endNodeNm("울산전통시장")
                        .mapDist(123)
                        .regionCode(regionCode1)
                        .roadTraffic(roadTraffic1)
                        .build());


        RoadTraffic roadTraffic2 = roadTrafficRepository.save(
                RoadTraffic
                        .builder()
                        .linkId("2")
                        .prcsSpd(8)
                        .prcsTrvTime(3)
                        .build());
        RegionCode regionCode2 = regionCodeRepository.save(
                RegionCode
                        .builder()
                        .regCode(2)
                        .regName("울산동구")
                        .build());
        roadStatusRepository.save(
                RoadStatus
                        .builder()
                        .linkId("2")
                        .roadName("방어진로")
                        .startNodeNm("구청앞")
                        .endNodeNm("울산동구 마을센터")
                        .mapDist(199)
                        .regionCode(regionCode2)
                        .roadTraffic(roadTraffic2)
                        .build());
        em.flush();
        em.clear();

        // when
        List<RoadStatus> roadStatuses = roadStatusRepository.findAll();

        for (RoadStatus roadStatus : roadStatuses) {
            System.out.println(roadStatus.getRegionCode().getClass());
            System.out.println("발생위치 : " + roadStatus.getRegionCode().getRegName());
        }
        // then
    }
}