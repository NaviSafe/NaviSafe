package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.geometry.TmapGeometry;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.geometry.TmapGeometryDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class TmapFeature {

    private String type;

    @JsonDeserialize(using = TmapGeometryDeserializer.class)
    private TmapGeometry geometry;

    private TmapProperties properties;
}
