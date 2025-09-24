package com.naviSafe.naviSafe.domain.outbreak.controller;

import com.naviSafe.naviSafe.domain.outbreak.dto.OutbreakResponseDto;
import com.naviSafe.naviSafe.domain.outbreak.entitiy.Outbreak;
import com.naviSafe.naviSafe.domain.outbreak.service.OutbreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OutbreakController {

    private final OutbreakService outbreakService;

    @Autowired
    public OutbreakController(OutbreakService outbreakService) {
        this.outbreakService = outbreakService;
    }

    @GetMapping("/api/naviSafe/accInfo")
    public ResponseEntity<?> getAllOutbreak(){
        List<Outbreak> outbreakList = outbreakService.findAll();

        List<OutbreakResponseDto> responseDtoList = outbreakList.stream()
                .map(o -> OutbreakResponseDto.builder()
                        .accId(o.getAccId())
                        .accInfo(o.getAccInfo())
                        .grs80tmX(o.getGrs80tmX())
                        .grs80tmY(o.getGrs80tmY())
                        .occrDate(o.getOccrDate())
                        .expClrDate(o.getExpClrDate())
                        .accTypeName(o.getOutbreakCode().getAccTypeNM())
                        .accDetailTypeName(o.getOutbreakDetailCode().getAccTypeNM())
                        .roadName(o.getRoadStatus().getRoadName())
                        .startNodeName(o.getRoadStatus().getStartNodeNm())
                        .endNodeName(o.getRoadStatus().getEndNodeNm())
                        .mapDistance(o.getRoadStatus().getMapDist())
                        .speedLoadTraffic(o.getRoadStatus().getRoadTraffic().getPrcsSpd())
                        .travelTimeLoad(o.getRoadStatus().getRoadTraffic().getPrcsTrvTime())
                        .regionName(o.getRoadStatus().getRegionCode().getRegName())
                        .build()
                )
                .toList();

        return ResponseEntity.ok(responseDtoList);
    }
}
