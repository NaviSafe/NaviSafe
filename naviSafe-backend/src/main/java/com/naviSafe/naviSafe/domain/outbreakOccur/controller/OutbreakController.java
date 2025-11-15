package com.naviSafe.naviSafe.domain.outbreakOccur.controller;

import com.naviSafe.naviSafe.domain.outbreakOccur.dto.OutbreakResponseDto;
import com.naviSafe.naviSafe.domain.outbreakOccur.entitiy.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.service.OutbreakService;
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
        List<OutbreakOccur> outbreakList = outbreakService.findAll();

        List<OutbreakResponseDto> responseDtoList = outbreakList.stream()
                .map(o -> OutbreakResponseDto.builder()
                        .accId(o.getAccId())
                        .accInfo(o.getAccidentAlert().getAccInfo())
                        .grs80tmX(o.getOutbreakMapGps().getGrs80tmX())
                        .grs80tmY(o.getOutbreakMapGps().getGrs80tmY())
                        .occrDate(o.getOccrDate())
                        .expClrDate(o.getExpClrDate())
                        .accTypeName(o.getOutbreakCode().getOutbreakCodeName().getAccTypeNM())
                        .accDetailTypeName(o.getOutbreakDetailCode().getOutbreakDetailCodeName().getAccTypeNM())
                        .roadName(o.getRoadStatusLink().getRoadStatus().getRoadName())
                        .startNodeName(o.getRoadStatusLink().getRoadStatus().getStartNodeNm())
                        .endNodeName(o.getRoadStatusLink().getRoadStatus().getEndNodeNm())
                        .mapDistance(o.getRoadStatusLink().getRoadStatus().getMapDist())
                        .speedLoadTraffic(o.getRoadStatusLink().getRoadTraffic().getPrcsSpd())
                        .travelTimeLoad(o.getRoadStatusLink().getRoadTraffic().getPrcsTrvTime())
                        .regionName(o.getRoadStatusLink().getRoadStatus().getRegionCode().getRegName())
                        .build()
                )
                .toList();

        return ResponseEntity.ok(responseDtoList);
    }
}
