package com.naviSafe.naviSafe.domain.MyRootPath.v2.dto;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.Position;

public record RouteEdge(
        Integer seq,
        Long edge,
        MultiLineString<Position> geom,
        Double cost,
        Double aggCost
) {}