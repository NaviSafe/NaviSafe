package com.naviSafe.naviSafe.domain.MyRootPath.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;


@Component
@RequiredArgsConstructor
public class DangerZoneSelector {

    public static final double EPS = 5000;   // meters
    private static final int MIN_PTS = 2;
    private static final int MAX_CENTERS = 5;

    private final DbscanClusterer clusterer;

    public List<Point> selectCenters(List<Point> dangerPoints) {

        List<List<Point>> clusters =
                clusterer.cluster(dangerPoints, EPS, MIN_PTS);

        return clusters.stream()
                .sorted(Comparator.<List<Point>>comparingInt(List::size).reversed())
                .limit(MAX_CENTERS)
                .map(this::centerOf)
                .toList();
    }

    private Point centerOf(List<Point> cluster) {
        double lat = cluster.stream().mapToDouble(Point::lat).average().orElse(0);
        double lon = cluster.stream().mapToDouble(Point::lon).average().orElse(0);
        return new Point(lat, lon);
    }
}
