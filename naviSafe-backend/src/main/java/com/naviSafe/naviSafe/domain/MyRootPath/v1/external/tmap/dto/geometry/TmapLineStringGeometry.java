package com.naviSafe.naviSafe.domain.MyRootPath.v1.external.tmap.dto.geometry;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TmapLineStringGeometry implements TmapGeometry {
    private String type;
    private List<List<Double>> coordinates;

}
