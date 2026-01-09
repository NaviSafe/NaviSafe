package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TmapProperties {

    private Integer totalDistance;
    private Integer totalTime;

    private Integer distance;
    private Integer time;

    private String pointType;

    private String name;
    private String description;
    private String nextRoadName;
}
