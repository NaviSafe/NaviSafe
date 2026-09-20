package com.naviSafe.naviSafe.domain.MyRootPath.v2.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class StartEndCoordRequestDto {
    private double fromLongitude;
    private double fromLatitude;
    private double toLongitude;
    private double toLatitude;
    private List<String> excludeOutbreakTypeName;
}
