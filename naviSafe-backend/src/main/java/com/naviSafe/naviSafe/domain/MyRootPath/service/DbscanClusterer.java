package com.naviSafe.naviSafe.domain.MyRootPath.service;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DbscanClusterer {

    public List<List<Point>> cluster(
            List<Point> points,
            double eps,
            int minPts
    ) {
        List<List<Point>> clusters = new ArrayList<>();
        Set<Point> visited = new HashSet<>();

        for (Point p : points) {
            if (visited.contains(p)) continue;
            visited.add(p);

            List<Point> neighbors = regionQuery(points, p, eps);
            if (neighbors.size() < minPts) continue;

            List<Point> cluster = new ArrayList<>();
            expand(cluster, neighbors, points, visited, eps, minPts);
            clusters.add(cluster);
        }
        return clusters;
    }

    private void expand(
            List<Point> cluster,
            List<Point> neighbors,
            List<Point> points,
            Set<Point> visited,
            double eps,
            int minPts
    ) {

        for (Point p : neighbors) {
            if (!cluster.contains(p)) {
                cluster.add(p); // cluster에 바로 추가
            }
        }

        for (int i = 0; i < neighbors.size(); i++) {
            Point p = neighbors.get(i);
            if (!visited.contains(p)) {
                visited.add(p);
                List<Point> n2 = regionQuery(points, p, eps);
                if (n2.size() >= minPts) {
                    for (Point np : n2) {
                        if (!neighbors.contains(np)) {
                            neighbors.add(np);  // neighbors에도 추가
                        }
                        if (!cluster.contains(np)) {
                            cluster.add(np);    // cluster에도 바로 추가
                        }
                    }
                }
            }
        }
    }

    private List<Point> regionQuery(List<Point> points, Point center, double eps) {
        List<Point> result = new ArrayList<>();
        for (Point p : points) {
            if (GeoCalculator.distance(center, p) <= eps) {
                result.add(p);
            }
        }
        return result;
    }
}
