package com.naviSafe.naviSafe.domain.outbreak.repository;

import com.naviSafe.naviSafe.domain.outbreak.entitiy.Outbreak;
import com.naviSafe.naviSafe.domain.outbreakCode.entitiy.OutbreakCode;
import com.naviSafe.naviSafe.domain.outbreakCode.repository.OutbreakCodeRepository;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCode;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.repository.OutbreakDetailCodeRepository;
import com.naviSafe.naviSafe.domain.regionCode.entitiy.RegionCode;
import com.naviSafe.naviSafe.domain.regionCode.repository.RegionCodeRepository;
import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatus;
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
import java.util.NoSuchElementException;

@SpringBootTest
class OutbreakRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(OutbreakRepositoryTest.class);
    private final OutbreakRepository outbreakRepository;
    private final RoadStatusRepository roadStatusRepository;
    private final RoadTrafficRepository roadTrafficRepository;
    private final RegionCodeRepository regionCodeRepository;

    private final OutbreakCodeRepository outbreakCodeRepository;
    private final OutbreakDetailCodeRepository outbreakDetailCodeRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public OutbreakRepositoryTest(OutbreakRepository outbreakRepository, RoadStatusRepository roadStatusRepository, RoadTrafficRepository roadTrafficRepository, RegionCodeRepository regionCodeRepository, OutbreakCodeRepository outbreakCodeRepository, OutbreakDetailCodeRepository outbreakDetailCodeRepository) {
        this.outbreakRepository = outbreakRepository;
        this.roadStatusRepository = roadStatusRepository;
        this.roadTrafficRepository = roadTrafficRepository;
        this.regionCodeRepository = regionCodeRepository;
        this.outbreakCodeRepository = outbreakCodeRepository;
        this.outbreakDetailCodeRepository = outbreakDetailCodeRepository;
    }

    @Test
    @Transactional
    @DisplayName("accId로 조회해서 전체 엔티티 정보 확인")
    public void findTest () throws Exception {
        //given
        Outbreak findOutbreak = outbreakRepository.findByAccId("1029246")
                .orElseThrow(() -> new NoSuchElementException("해당 accId를 찾울 수 없습니다"));

        // when
        log.info(findOutbreak.toString());

        // then
        log.info("==== Outbreak ====");
        log.info("accId         : {}", findOutbreak.getAccId());
        log.info("발생일        : {}", findOutbreak.getOccrDate());
        log.info("해제예정일    : {}", findOutbreak.getExpClrDate());
        log.info("사고정보      : {}", findOutbreak.getAccInfo());
        log.info("좌표(X, Y)    : {}, {}", findOutbreak.getGrs80tmX(), findOutbreak.getGrs80tmY());

        log.info("==== RoadStatus ====");
        RoadStatus roadStatus = findOutbreak.getRoadStatus();
        log.info("링크ID      : {}", roadStatus.getLinkId());
        log.info("도로명        : {}", roadStatus.getRoadName());

        log.info("==== roadTraffic ====");
        RoadTraffic roadTraffic = roadStatus.getRoadTraffic();
        log.info("속도          : {}", roadTraffic.getPrcsSpd());
        log.info("여행시간       :{}", roadTraffic.getPrcsTrvTime());

        log.info("==== OutbreakCode ====");
        OutbreakCode outbreakCode = findOutbreak.getOutbreakCode();
        log.info("코드          : {}", outbreakCode.getAccType());
        log.info("코드명        : {}", outbreakCode.getAccTypeNM());

        log.info("==== OutbreakDetailCode ====");
        OutbreakDetailCode outbreakDetailCode = findOutbreak.getOutbreakDetailCode();
        log.info("세부코드      : {}", outbreakDetailCode.getAccType());
        log.info("세부코드명    : {}", outbreakDetailCode.getAccTypeNM());


    }


    @Test
    @Transactional
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
                        .roadTraffic(roadTraffic1)
                        .build());

        OutbreakCode outbreakCode1 = outbreakCodeRepository.save(
                OutbreakCode
                        .builder()
                        .accType("01")
                        .accTypeNM("공사")
                        .build()
        );

        OutbreakDetailCode outbreakDetailCode1 = outbreakDetailCodeRepository.save(
                OutbreakDetailCode.builder()
                        .accType("01A")
                        .accTypeNM("공사세부")
                        .build()
        );

        outbreakRepository.save(
                Outbreak.builder()
                        .accId("1")
                        .occrDate(ZonedDateTime.of(2025, 6, 22, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
                        .expClrDate(ZonedDateTime.of(2026, 6, 22, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
                        .accInfo("공사")
                        .grs80tmX(1.0f)
                        .grs80tmY(1.0f)
                        .roadStatus(roadStatus1)
                        .outbreakCode(outbreakCode1)
                        .outbreakDetailCode(outbreakDetailCode1)
                        .build()
        );


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
        RoadStatus roadStatus2 = roadStatusRepository.save(
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

        OutbreakCode outbreakCode2 = outbreakCodeRepository.save(
                OutbreakCode
                        .builder()
                        .accType("02")
                        .accTypeNM("사고")
                        .build()
        );

        OutbreakDetailCode outbreakDetailCode2 = outbreakDetailCodeRepository.save(
                OutbreakDetailCode.builder()
                        .accType("02A")
                        .accTypeNM("추돌사고")
                        .build()
        );

        outbreakRepository.save(
                Outbreak.builder()
                        .accId("2")
                        .occrDate(ZonedDateTime.of(2025, 6, 23, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
                        .expClrDate(ZonedDateTime.of(2026, 6, 23, 0,0,0, 0, ZoneId.of("Asia/Seoul")))
                        .accInfo("사고")
                        .grs80tmX(2.0f)
                        .grs80tmY(2.0f)
                        .roadStatus(roadStatus2)
                        .outbreakCode(outbreakCode2)
                        .outbreakDetailCode(outbreakDetailCode2)
                        .build()
        );

        em.flush();
        em.clear();

        // when
        List<Outbreak> findAll = outbreakRepository.findAll();

        for (Outbreak outbreak : findAll) {
            System.out.println("돌발상황 = " + outbreak.getOutbreakCode().getAccTypeNM());
            System.out.println("돌발상황세부 = " + outbreak.getOutbreakDetailCode().getAccTypeNM());
            System.out.println("돌발 발생 도로명 = " + outbreak.getRoadStatus().getRoadName());
            System.out.println("돌발 발생 도로 상황(교통속도) = " + outbreak.getRoadStatus().getRoadTraffic().getPrcsSpd());
            System.out.println("돌발 발생 지역 = " + outbreak.getRoadStatus().getRegionCode().getRegName());
        }

        // then
    }
}