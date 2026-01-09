package com.naviSafe.naviSafe.domain.MyRootPath.Utils;

import com.naviSafe.naviSafe.domain.MyRootPath.service.Point;

import java.util.List;
import java.util.stream.Collectors;

public class TmapRequestUtil {
    public static String buildPassListString(List<Point> viaPoints) {
        // 최대 5개까지만 사용
        return viaPoints.stream()
                .limit(5)
                .map(p -> p.lon() + "," + p.lat()) // X : lon,Y: lat 좌표 문자열
                .collect(Collectors.joining("_")); // _로 연결
    }
}
