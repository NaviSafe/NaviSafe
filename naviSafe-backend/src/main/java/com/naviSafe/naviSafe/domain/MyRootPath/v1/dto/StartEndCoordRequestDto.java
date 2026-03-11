package com.naviSafe.naviSafe.domain.MyRootPath.v1.dto;

import lombok.Getter;

@Getter
public class StartEndCoordRequestDto {
    private double fromLongitude;
    private double fromLatitude;
    private double toLongitude;
    private double toLatitude;
}
