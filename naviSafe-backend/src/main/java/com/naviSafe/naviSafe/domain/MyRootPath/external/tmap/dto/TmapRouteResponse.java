package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class TmapRouteResponse {

    private String type;
    private List<TmapFeature> features;
}
