package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.geometry;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TmapPointGeometry implements TmapGeometry {

    private String type;
    private List<Double> coordinates;
}
