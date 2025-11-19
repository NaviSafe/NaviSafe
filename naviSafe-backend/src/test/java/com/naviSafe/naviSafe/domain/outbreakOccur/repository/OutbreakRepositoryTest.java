package com.naviSafe.naviSafe.domain.outbreakOccur.repository;

import com.naviSafe.naviSafe.domain.accidentAlert.entity.AccidentAlert;
import com.naviSafe.naviSafe.domain.accidentAlert.repository.AccidentAlertRepository;
import com.naviSafe.naviSafe.domain.outbreakCode.entity.OutbreakCodeName;
import com.naviSafe.naviSafe.domain.outbreakCode.repository.OutbreakCodeNameRepository;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entity.OutbreakDetailCodeName;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.repository.OutbreakDetailCodeNameRepository;
import com.naviSafe.naviSafe.domain.outbreakMapGPS.entity.OutbreakMapGps;
import com.naviSafe.naviSafe.domain.outbreakMapGPS.repository.OutbreakMapGpsRepository;
import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakCode.entity.OutbreakCode;
import com.naviSafe.naviSafe.domain.outbreakCode.repository.OutbreakCodeRepository;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entity.OutbreakDetailCode;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.repository.OutbreakDetailCodeRepository;
import com.naviSafe.naviSafe.domain.regionCode.entity.RegionCode;
import com.naviSafe.naviSafe.domain.regionCode.repository.RegionCodeRepository;
import com.naviSafe.naviSafe.domain.roadStatus.entity.RoadStatus;
import com.naviSafe.naviSafe.domain.roadStatus.entity.RoadStatusLink;
import com.naviSafe.naviSafe.domain.roadStatus.repository.RoadStatusLinkRepository;
import com.naviSafe.naviSafe.domain.roadStatus.repository.RoadStatusRepository;
import com.naviSafe.naviSafe.domain.roadTraffic.entity.RoadTraffic;
import com.naviSafe.naviSafe.domain.roadTraffic.repository.RoadTrafficRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@SpringBootTest
class OutbreakRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(OutbreakRepositoryTest.class);
    private final OutbreakRepository outbreakRepository;

    private final RoadStatusRepository roadStatusRepository;
    private final RoadStatusLinkRepository roadStatusLinkRepository;

    private final RoadTrafficRepository roadTrafficRepository;
    private final RegionCodeRepository regionCodeRepository;

    private final OutbreakCodeRepository outbreakCodeRepository;
    private final OutbreakCodeNameRepository outbreakCodeNameRepository;
    private final OutbreakDetailCodeRepository outbreakDetailCodeRepository;
    private final OutbreakDetailCodeNameRepository outbreakDetailCodeNameRepository;

    private final OutbreakMapGpsRepository outbreakMapGpsRepository;

    private final AccidentAlertRepository accidentAlertRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public OutbreakRepositoryTest(OutbreakRepository outbreakRepository, RoadStatusRepository roadStatusRepository, RoadStatusLinkRepository roadStatusLinkRepository, RoadTrafficRepository roadTrafficRepository, RegionCodeRepository regionCodeRepository, OutbreakCodeRepository outbreakCodeRepository, OutbreakCodeNameRepository outbreakCodeNameRepository, OutbreakDetailCodeRepository outbreakDetailCodeRepository, OutbreakDetailCodeNameRepository outbreakDetailCodeNameRepository, OutbreakMapGpsRepository outbreakMapGpsRepository, AccidentAlertRepository accidentAlertRepository) {
        this.outbreakRepository = outbreakRepository;
        this.roadStatusRepository = roadStatusRepository;
        this.roadStatusLinkRepository = roadStatusLinkRepository;
        this.roadTrafficRepository = roadTrafficRepository;
        this.regionCodeRepository = regionCodeRepository;
        this.outbreakCodeRepository = outbreakCodeRepository;
        this.outbreakCodeNameRepository = outbreakCodeNameRepository;
        this.outbreakDetailCodeRepository = outbreakDetailCodeRepository;
        this.outbreakDetailCodeNameRepository = outbreakDetailCodeNameRepository;
        this.outbreakMapGpsRepository = outbreakMapGpsRepository;
        this.accidentAlertRepository = accidentAlertRepository;
    }

    @Test
    @DisplayName("엔티티 그래프를 통한 전체 조회 테스트")
    public void findAllOutbreakByEntityGraph () throws Exception {
        //given
        RoadTraffic roadTraffic1 = roadTrafficRepository.save(
                RoadTraffic
                        .builder()
                        .linkId("1180001100")
                        .prcsSpd(10)
                        .prcsTrvTime(2)
                        .build());
        RegionCode regionCode1 = regionCodeRepository.save(
                RegionCode
                        .builder()
                        .regCode(118)
                        .regName("영등포구")
                        .build());

        RoadStatus roadStatus1 = roadStatusRepository.save(
                RoadStatus
                        .builder()
                        .linkId("1180001100")
                        .regCdRegCd(118)
                        .roadName("경인로")
                        .startNodeNm("서울교남단")
                        .endNodeNm("영등포로타리")
                        .mapDist(273)
                        .regionCode(regionCode1)
                        .build());

        RoadStatusLink roadStatusLink1 = roadStatusLinkRepository.save(
                RoadStatusLink.builder()
                        .outbreakAccId("1029246")
                        .linkId("1180001100")
                        .roadStatus(roadStatus1)
                        .build()
        );

        OutbreakCodeName outbreakCodeName1 = outbreakCodeNameRepository.save(
                OutbreakCodeName
                        .builder()
                        .accType("A04")
                        .accTypeNM("공사")
                        .build()
        );

        OutbreakCode outbreakCode1 = outbreakCodeRepository.save(
                OutbreakCode
                        .builder()
                        .outbreakAccId("1029246")
                        .accType("A04")
                        .outbreakCodeName(outbreakCodeName1)
                        .build()
        );

        OutbreakDetailCodeName outbreakDetailCodeName1 = outbreakDetailCodeNameRepository.save(
                OutbreakDetailCodeName.builder()
                        .accDtype("04B01")
                        .accTypeNM("시설물보수")
                        .build()
        );

        OutbreakDetailCode outbreakDetailCode1 = outbreakDetailCodeRepository.save(
                OutbreakDetailCode.builder()
                        .outbreakAccId("1029246")
                        .accDtype("04B01")
                        .outbreakDetailCodeName(outbreakDetailCodeName1)
                        .build()
        );

        OutbreakMapGps mapGps1 = outbreakMapGpsRepository.save(
                OutbreakMapGps.builder()
                        .outbreakAccId("1029246")
                        .grs80tmX(192385.0401850186)
                        .grs80tmY(446645.5247824667)
                        .build()
        );

        AccidentAlert accident1 = accidentAlertRepository.save(
                AccidentAlert.builder()
                        .outbreakAccId("1029246")
                        .accInfo("경인로 (영등포로터리 → 서울교남단) 구간 영등포고가 철거관련 공사 시설물보수")
                        .build()
        );


        outbreakRepository.save(
                OutbreakOccur.builder()
                        .accId("1029246")
                        .occrDate(ZonedDateTime.of(2025, 6, 10, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
                        .expClrDate(ZonedDateTime.of(2026, 6, 30, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
                        .roadStatusLink(roadStatusLink1)
                        .outbreakCode(outbreakCode1)
                        .outbreakDetailCode(outbreakDetailCode1)
                        .outbreakMapGps(mapGps1)
                        .accidentAlert(accident1)
                        .build()
        );

        em.flush();
        em.clear();

        // when
        List<OutbreakOccur> findAll = outbreakRepository.findAll();

        for (OutbreakOccur outbreak : findAll) {
            System.out.println("돌발상황 = " + outbreak.getOutbreakCode().getOutbreakCodeName().getAccTypeNM());
            System.out.println("돌발상황세부 = " + outbreak.getOutbreakDetailCode().getOutbreakDetailCodeName().getAccTypeNM());
            System.out.println("돌발 발생 도로명 = " + outbreak.getRoadStatusLink().getRoadStatus().getRoadName());
            System.out.println("돌발 발생 지역 = " + outbreak.getRoadStatusLink().getRoadStatus().getRegionCode().getRegName());
        }

        // then
    }
}