package com.naviSafe.naviSafe.domain.rain.repository;

import com.naviSafe.naviSafe.domain.rain.entitiy.Rain;
import com.naviSafe.naviSafe.domain.region.entitiy.Region;
import com.naviSafe.naviSafe.domain.region.repository.RegionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RainRepositoryTest {

    @Autowired
    private RainRepository rainRepository;

    @Autowired
    private RegionRepository regionRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("전체 조회")
    public void findAllTest () throws Exception {
        //given
        Region r1 = Region.builder().guName("송파구").guCode(125).build();
        Region r2 = Region.builder().guName("강남구").guCode(101).build();
        Region r3 = Region.builder().guName("강동구").guCode(102).build();
        Region r4 = Region.builder().guName("도봉구").guCode(103).build();
        Region r5 = Region.builder().guName("노원구").guCode(104).build();

        List<Region> regionList = new ArrayList<>();
        regionList.add(r1);
        regionList.add(r2);
        regionList.add(r3);
        regionList.add(r4);
        regionList.add(r5);

        regionRepository.saveAll(regionList);

        rainRepository.save(Rain.builder().guCode(125).rainFall10(0).receiveTime("2025-09-26 08:39").region(r1).build());
        rainRepository.save(Rain.builder().guCode(101).rainFall10(0).receiveTime("2025-09-26 08:39").region(r2).build());
        rainRepository.save(Rain.builder().guCode(102).rainFall10(0).receiveTime("2025-09-26 08:39").region(r3).build());
        rainRepository.save(Rain.builder().guCode(103).rainFall10(0).receiveTime("2025-09-26 08:39").region(r4).build());
        rainRepository.save(Rain.builder().guCode(104).rainFall10(0).receiveTime("2025-09-26 08:39").region(r5).build());

        em.flush();
        em.clear();
        // when

        List<Rain> rainList = rainRepository.findAll();

        for (Rain rain : rainList) {
            System.out.println("구 이름 = " + rain.getRegion().getGuName());
            System.out.println("10분 강수량 = " + rain.getRainFall10());
        }
        // then
    }
}