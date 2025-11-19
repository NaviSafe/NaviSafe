package com.naviSafe.naviSafe.domain.shelter.type.service;

import com.naviSafe.naviSafe.domain.shelter.gps.entity.ShelterGps;
import com.naviSafe.naviSafe.domain.shelter.gps.repository.ShelterGpsRepository;
import com.naviSafe.naviSafe.domain.shelter.type.entity.ShelterType;
import com.naviSafe.naviSafe.domain.shelter.type.repository.ShelterTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShelterTypeServiceTest {

    private final ShelterTypeService shelterTypeService;
    private final ShelterTypeRepository shelterTypeRepository;
    private final ShelterGpsRepository shelterGpsRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    public ShelterTypeServiceTest(ShelterTypeService shelterTypeService, ShelterTypeRepository shelterTypeRepository, ShelterGpsRepository shelterGpsRepository) {
        this.shelterTypeService = shelterTypeService;
        this.shelterTypeRepository = shelterTypeRepository;
        this.shelterGpsRepository = shelterGpsRepository;
    }

    @Test
    @Transactional
    void getShelterType() {
        //given
        ShelterGps a = shelterGpsRepository.save(
                ShelterGps.builder()
                        .shelterCode(1)
                        .shelterAddress("A-1")
                        .shelterName("A")
                        .lat(1.0f)
                        .lot(1.0f)
                        .build()
        );

        ShelterGps b = shelterGpsRepository.save(
                ShelterGps.builder()
                        .shelterCode(1)
                        .shelterAddress("B-1")
                        .shelterName("B")
                        .lat(1.0f)
                        .lot(1.0f)
                        .build()
        );
        ShelterGps c = shelterGpsRepository.save(
                ShelterGps.builder()
                        .shelterCode(1)
                        .shelterAddress("C-1")
                        .shelterName("C")
                        .lat(1.0f)
                        .lot(1.0f)
                        .build()
        );

        ShelterGps d = shelterGpsRepository.save(
                ShelterGps.builder()
                        .shelterCode(2)
                        .shelterAddress("D-1")
                        .shelterName("D")
                        .lat(1.0f)
                        .lot(1.0f)
                        .build()
        );

        em.flush();
        em.clear();

        List<ShelterGps> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);

        shelterTypeRepository.save(
                ShelterType.builder()
                        .shelterCode(1)
                        .shelterCodeName("지진")
                        .shelterGpsList(list)
                        .build()
        );

        em.flush();
        em.clear();


        //when

        Optional<ShelterType> byShelterCode = shelterTypeService.findByShelterCode(1);

        //then
        ShelterType shelterType = byShelterCode
                .orElseThrow(() -> new RuntimeException("ShelterType not found"));

        List<ShelterGps> shelterGpsList = shelterType.getShelterGpsList();

        for (ShelterGps shelterGps : shelterGpsList) {
            System.out.println(shelterGps.toString());
        }

        assertTrue(byShelterCode.isPresent());


    }
}