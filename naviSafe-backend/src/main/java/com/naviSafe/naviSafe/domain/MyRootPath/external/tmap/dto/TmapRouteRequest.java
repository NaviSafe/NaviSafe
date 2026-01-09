package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmapRouteRequest {
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private String passList;
}
