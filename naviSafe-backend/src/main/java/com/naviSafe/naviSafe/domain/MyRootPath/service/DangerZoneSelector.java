package com.naviSafe.naviSafe.domain.MyRootPath.service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

import static com.naviSafe.naviSafe.domain.MyRootPath.service.GeoCalculator.distance;


@Component
@RequiredArgsConstructor
public class DangerZoneSelector {

    public static final double EPS = 200;   // meters
    private static final int MIN_PTS = 2;
    private static final int MIN_AVOID_RADIUS = 500;

    private final DbscanClusterer clusterer;
    Logger logger = LoggerFactory.getLogger(DangerZoneSelector.class);

    /**
     * 기존 경로(Route Polyline)와 인접한 돌발상황(Danger Point)을 기반으로
     * 회피 경유지(Avoid Point)를 생성하는 책임을 가진 클래스.
     *
     * 핵심 목적
     * - 돌발상황으로 인해 기존 경로가 위험한 경우,
     *   경로를 크게 이탈하지 않으면서도 안전한 우회 경유지를 생성한다.
     *
     * 전체 알고리즘 흐름
     * 1. 돌발상황 필터링
     *    - 기존 경로 좌표들과 일정 반경(IMPACT_RADIUS) 이내에 있는
     *      돌발상황만 추출한다.
     *    - 경로와 충분히 멀리 떨어진 돌발상황은 회피 대상에서 제외한다.
     *
     * 2. DBSCAN 클러스터링
     *    - 필터링된 돌발상황들을 DBSCAN으로 군집화한다.
     *    - 서로 가까운 돌발상황들을 하나의 위험 구간(클러스터)으로 묶는다.
     *
     * 3. 클러스터링 실패 시 fallback 처리
     *    - 클러스터가 하나도 생성되지 않는 경우,
     *      (돌발상황 간 거리가 멀어 군집이 형성되지 않는 경우)
     *    - 경로와 가장 가까운 돌발상황 순으로 정렬 후
     *      최대 5개까지만 선택하여
     *      각 돌발상황을 단일 클러스터처럼 처리한다.
     *
     * 4. 회피 지점 생성
     *    - 각 클러스터(또는 단일 돌발상황)에 대해 회피 지점을 생성한다.
     *    - 경로 진행 방향 벡터를 계산하고,
     *      그에 대한 법선 벡터 방향으로 이동시킨 좌표를 경유지로 사용한다.
     *
     * 5. 이동 거리 결정
     *    - 클러스터 반경(클러스터 내 최대 거리)을 기준으로 회피 거리 산정
     *    - 반경이 너무 작은 경우 최소 회피 반경(MIN_AVOID_RADIUS)으로 보정
     *
     * 최종 결과
     * - 생성된 회피 지점들을 Tmap 경유지로 사용하여
     *   안전한 새로운 경로 탐색에 활용한다.
     */
    public List<Point> selectAvoidPoints(List<Point> dangerPoints, List<Point> routePolylinePoints) {
        // 돌발상황들과, 가장 먼저 가져온 Tmap 경로 상 필터링 수행
        double IMPACT_RADIUS = 200;

        List<Point> filteredDangers = dangerPoints.stream()
                .filter(danger ->
                        routePolylinePoints.stream()
                                .anyMatch(routePoint ->
                                        distance(danger, routePoint) <= IMPACT_RADIUS
                                )
                )
                .toList();
        if (filteredDangers.isEmpty()) {
            logger.info("기존 경로 상에 인접하는 돌발상황이 없습니다.");
            return List.of();
        }

        // 클러스터링
        List<List<Point>> clusters = clusterer.cluster(filteredDangers, EPS, MIN_PTS);
        logger.info("클러스터링 결과 : {}", clusters.toString());

        // 클러스터링이 실패 시 대응
        if(clusters.isEmpty()) {
            logger.info("클러스터 생성 실패 → fallback 회피지점 생성");
            return filteredDangers.stream()
                    // 경로와 가장 가까운 위험부터
                    .sorted(Comparator.comparingDouble(
                            d -> minDistanceToRoute(d, routePolylinePoints)
                    ))
                    // Tmap 경유지 최대 5개
                    .limit(5)
                    // 단일 위험을 하나의 클러스터처럼 처리
                    .map(danger ->
                            createAvoidPoint(List.of(danger), routePolylinePoints)
                    )
                    .toList();
        }

        return clusters.stream()
                .map(cluster ->
                        createAvoidPoint(cluster, routePolylinePoints)
                )
                .toList();
    }

    private Point createAvoidPoint(
            List<Point> cluster,
            List<Point> routePolylinePoints
    ) {
        // 1. 기준 위험점 P
        Point dangerP = selectClosestDangerToRoute(cluster, routePolylinePoints);

        // 2. P와 가장 가까운 경로 점 R
        Point routePoint = routePolylinePoints.stream()
                .min(Comparator.comparingDouble(
                        r -> GeoCalculator.distance(dangerP, r)
                ))
                .orElseThrow();

        // 3. 경로 진행 벡터 계산
        int idx = routePolylinePoints.indexOf(routePoint);
        Point prev = (idx > 0) ? routePolylinePoints.get(idx - 1) : routePoint;
        Point next = (idx < routePolylinePoints.size() - 1)
                ? routePolylinePoints.get(idx + 1)
                : routePoint;

        Vector2D direction = new Vector2D(
                next.lon() - prev.lon(),
                next.lat() - prev.lat()
        ).normalize();

        // 4. 법선 벡터
        Vector2D normal = new Vector2D(
                -direction.y(),
                direction.x()
        ).normalize();

        // 5. 위험에서 멀어지는 법선 선택
        Vector2D dangerToRoute = new Vector2D(
                routePoint.lon() - dangerP.lon(),
                routePoint.lat() - dangerP.lat()
        );

        if (normal.dot(dangerToRoute) > 0) {
            normal = normal.multiply(-1);
        }

        // 6. 이동 거리 (클러스터 반경 + 버퍼)
        double clusterRadius = cluster.stream()
                .mapToDouble(p -> GeoCalculator.distance(p, dangerP))
                .max()
                .orElse(0);

        // 단일 돌발상황(또는 매우 밀집된 경우) 최소 회피 반경 보정
        if (clusterRadius < MIN_AVOID_RADIUS) {
            clusterRadius = MIN_AVOID_RADIUS; // meters
        }

        double MOVE_DISTANCE = clusterRadius + 100; // buffer 100m

        logger.info("총 이동 거리 : {}", MOVE_DISTANCE);
        double meterToDegree = MOVE_DISTANCE / 111_000.0;

        // 7. 최종 회피 지점
        return new Point(
                dangerP.lat() + normal.y() * meterToDegree,
                dangerP.lon() + normal.x() * meterToDegree
        );
    }

    private Point selectClosestDangerToRoute(
            List<Point> cluster,
            List<Point> routePolylinePoints
    ) {
        return cluster.stream()
                .min(Comparator.comparingDouble(
                        danger -> minDistanceToRoute(danger, routePolylinePoints)
                ))
                .orElseThrow();
    }

    private double minDistanceToRoute(
            Point danger,
            List<Point> routePolylinePoints
    ) {
        return routePolylinePoints.stream()
                .mapToDouble(routePoint -> distance(danger, routePoint))
                .min()
                .orElse(Double.MAX_VALUE);
    }

}
