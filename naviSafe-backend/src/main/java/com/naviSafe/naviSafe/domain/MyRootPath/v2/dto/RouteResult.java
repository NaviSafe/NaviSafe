package com.naviSafe.naviSafe.domain.MyRootPath.v2.dto;

import java.util.List;

public record RouteResult(
        List<Point> points,
        double distance
) {}
