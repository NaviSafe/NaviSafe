package com.naviSafe.naviSafe.domain.outbreakOccur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@Builder
public class OutbreakResponseDto {
    private String accId;
    private ZonedDateTime occrDate;
    private ZonedDateTime expClrDate;
    private String accInfo;
    private double grs80tmX;
    private double grs80tmY;

    private String accTypeName;
    private String accDetailTypeName;

    private String roadName;
    private String startNodeName;
    private String endNodeName;
    private int mapDistance;

    private int speedLoadTraffic;
    private int travelTimeLoad;

    private String regionName;
}
