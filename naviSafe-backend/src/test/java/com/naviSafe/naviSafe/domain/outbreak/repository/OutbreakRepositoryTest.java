package com.naviSafe.naviSafe.domain.outbreak.repository;

import com.naviSafe.naviSafe.domain.outbreak.entitiy.Outbreak;
import com.naviSafe.naviSafe.domain.outbreakCode.entitiy.OutbreakCode;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCode;
import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatus;
import com.naviSafe.naviSafe.domain.roadTraffic.entitiy.RoadTraffic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@SpringBootTest
class OutbreakRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(OutbreakRepositoryTest.class);
    private final OutbreakRepository outbreakRepository;

    @Autowired
    public OutbreakRepositoryTest(OutbreakRepository outbreakRepository) {
        this.outbreakRepository = outbreakRepository;
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
}