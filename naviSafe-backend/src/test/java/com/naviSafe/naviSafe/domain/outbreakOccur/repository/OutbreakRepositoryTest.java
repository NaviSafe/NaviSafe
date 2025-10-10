package com.naviSafe.naviSafe.domain.outbreakOccur.repository;

import com.naviSafe.naviSafe.domain.accidentAlert.entity.AccidentAlert;
import com.naviSafe.naviSafe.domain.accidentAlert.repository.AccidentAlertRepository;
import com.naviSafe.naviSafe.domain.outbreakCode.entitiy.OutbreakCodeName;
import com.naviSafe.naviSafe.domain.outbreakCode.repository.OutbreakCodeNameRepository;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCodeName;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.repository.OutbreakDetailCodeNameRepository;
import com.naviSafe.naviSafe.domain.outbreakMapGPS.entity.OutbreakMapGps;
import com.naviSafe.naviSafe.domain.outbreakMapGPS.repository.OutbreakMapGpsRepository;
import com.naviSafe.naviSafe.domain.outbreakOccur.entitiy.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakCode.entitiy.OutbreakCode;
import com.naviSafe.naviSafe.domain.outbreakCode.repository.OutbreakCodeRepository;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCode;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.repository.OutbreakDetailCodeRepository;
import com.naviSafe.naviSafe.domain.regionCode.entitiy.RegionCode;
import com.naviSafe.naviSafe.domain.regionCode.repository.RegionCodeRepository;
import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatus;
import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatusLink;
import com.naviSafe.naviSafe.domain.roadStatus.repository.RoadStatusLinkRepository;
import com.naviSafe.naviSafe.domain.roadStatus.repository.RoadStatusRepository;
import com.naviSafe.naviSafe.domain.roadTraffic.entitiy.RoadTraffic;
import com.naviSafe.naviSafe.domain.roadTraffic.repository.RoadTrafficRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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

        RoadStatus roadStatus1 = roadStatusRepository.save(
                RoadStatus
                        .builder()
                        .linkId("1")
                        .roadName("돋질로")
                        .startNodeNm("시청앞")
                        .endNodeNm("울산전통시장")
                        .mapDist(123)
                        .regionCode(regionCode1)
                        .build());

        RoadStatusLink roadStatusLink1 = roadStatusLinkRepository.save(
                RoadStatusLink.builder()
                        .outbreakAccId("1")
                        .linkIdLinkId("1")
                        .roadStatus(roadStatus1)
                        .roadTraffic(roadTraffic1)
                        .build()
        );

        OutbreakCodeName outbreakCodeName1 = outbreakCodeNameRepository.save(
                OutbreakCodeName
                        .builder()
                        .accType("01")
                        .accTypeNM("공사")
                        .build()
        );

        OutbreakCode outbreakCode1 = outbreakCodeRepository.save(
                OutbreakCode
                        .builder()
                        .outbreakAccId("1")
                        .accType("01")
                        .outbreakCodeName(outbreakCodeName1)
                        .build()
        );

        OutbreakDetailCodeName outbreakDetailCodeName1 = outbreakDetailCodeNameRepository.save(
                OutbreakDetailCodeName.builder()
                        .accDtype("01A")
                        .accTypeNM("공사세부")
                        .build()
        );

        OutbreakDetailCode outbreakDetailCode1 = outbreakDetailCodeRepository.save(
                OutbreakDetailCode.builder()
                        .outbreakAccId("1")
                        .accDtype("01A")
                        .outbreakDetailCodeName(outbreakDetailCodeName1)
                        .build()
        );

        OutbreakMapGps mapGps1 = outbreakMapGpsRepository.save(
                OutbreakMapGps.builder()
                        .outbreakAccId("1")
                        .grs80tmX(10.0f)
                        .grs80tmY(10.2f)
                        .build()
        );

        AccidentAlert accident1 = accidentAlertRepository.save(
                AccidentAlert.builder()
                        .outbreakAccId("1")
                        .accInfo("울산 시청앞 사고부근 150m 방면 5종 추돌 발생")
                        .build()
        );


        outbreakRepository.save(
                OutbreakOccur.builder()
                        .accId("1")
                        .occrDate(ZonedDateTime.of(2025, 6, 22, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
                        .expClrDate(ZonedDateTime.of(2026, 6, 22, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
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
            System.out.println("돌발 발생 도로 상황(교통속도) = " + outbreak.getRoadStatusLink().getRoadTraffic().getPrcsSpd());
            System.out.println("돌발 발생 지역 = " + outbreak.getRoadStatusLink().getRoadStatus().getRegionCode().getRegName());
        }

        // then
    }
}