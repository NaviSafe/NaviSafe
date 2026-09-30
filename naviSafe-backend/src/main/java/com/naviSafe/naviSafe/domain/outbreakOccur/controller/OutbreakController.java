package com.naviSafe.naviSafe.domain.outbreakOccur.controller;

import com.naviSafe.naviSafe.domain.outbreakOccur.dto.OutbreakRequestDto;
import com.naviSafe.naviSafe.domain.outbreakOccur.dto.OutbreakResponseDto;
import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.service.OutbreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OutbreakController {

    private final OutbreakService outbreakService;

    @Autowired
    public OutbreakController(OutbreakService outbreakService) {
        this.outbreakService = outbreakService;
    }

    @PostMapping("/api/naviSafe/accInfo")
    public ResponseEntity<?> getOutbreakInfo(@RequestBody OutbreakRequestDto outbreakRequestDto){
        List<OutbreakOccur> outbreakList = outbreakService.findAll();

        List<OutbreakResponseDto> responseDtoList = outbreakList.stream()
                .filter(o -> !outbreakRequestDto.getExcludeAccTypeNames().contains(
                        o.getOutbreakCode()
                                .getOutbreakCodeName()
                                .getAccTypeNM()
                ))
                .map(o -> OutbreakResponseDto.builder()
                        .accId(o.getAccId())
                        .accInfo(o.getAccidentAlert().getAccInfo())
                        .grs80tmX(o.getOutbreakMapGps().getGrs80tmX())
                        .grs80tmY(o.getOutbreakMapGps().getGrs80tmY())
                        .expClrDate(o.getExpClrDate())
                        .accTypeName(o.getOutbreakCode().getOutbreakCodeName().getAccTypeNM())
                        .accDetailTypeName(o.getOutbreakDetailCode().getOutbreakDetailCodeName().getAccTypeNM())
                        .roadName(o.getRoadStatusLink().getRoadStatus().getRoadName())
                        .startNodeName(o.getRoadStatusLink().getRoadStatus().getStartNodeNm())
                        .endNodeName(o.getRoadStatusLink().getRoadStatus().getEndNodeNm())
                        .mapDistance(o.getRoadStatusLink().getRoadStatus().getMapDist())
                        .regionName(o.getRoadStatusLink().getRoadStatus().getRegionCode().getRegName())
                        .build()
                )
                .toList();

        return ResponseEntity.ok(responseDtoList);
    }
}
