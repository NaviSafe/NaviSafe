package com.naviSafe.naviSafe.domain.MyRootPath.Utils;

import com.naviSafe.naviSafe.domain.MyRootPath.service.GeoCalculator;
import com.naviSafe.naviSafe.domain.MyRootPath.service.Point;
import org.locationtech.proj4j.*;

import java.util.Comparator;
import java.util.List;

public class AvoidPointGenerator {

    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

    private static final CoordinateReferenceSystem WGS84 =
            crsFactory.createFromName("EPSG:4326");

    private static final CoordinateReferenceSystem EPSG2097 =
            crsFactory.createFromParameters(
                    "EPSG:2097",
                    "+proj=tmerc +lat_0=38 +lon_0=127 " +
                            "+k=1 +x_0=200000 +y_0=500000 " +
                            "+ellps=bessel +units=m +no_defs " +
                            "+towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43"
            );

    private static final CoordinateTransform TO_2097 =
            ctFactory.createTransform(WGS84, EPSG2097);

    private static final CoordinateTransform TO_WGS84 =
            ctFactory.createTransform(EPSG2097, WGS84);

    /**
     * 단일 돌발상황 회피 좌표 생성
     * @param danger 돌발상황 좌표
     * @param route 경로 polyline
     * @param distance 회피 거리(m)
     * @return 회피 좌표
     */
    public static Point generate(Point danger, List<Point> route, double distance) {
        ProjCoordinate dangerSrc = new ProjCoordinate(danger.lon(), danger.lat());
        ProjCoordinate dangerM = new ProjCoordinate();
        TO_2097.transform(dangerSrc, dangerM);

        // 1. 가장 가까운 경로점 찾기
        Point closest = route.stream()
                .min(Comparator.comparingDouble(p -> GeoCalculator.distance(danger, p)))
                .orElse(route.get(0));

        ProjCoordinate closestSrc = new ProjCoordinate(closest.lon(), closest.lat());
        ProjCoordinate closestM = new ProjCoordinate();
        TO_2097.transform(closestSrc, closestM);

        // 2. 방향 벡터 계산
        double dx = closestM.x - dangerM.x;
        double dy = closestM.y - dangerM.y;

        double length = Math.sqrt(dx*dx + dy*dy);

        // 3. 벡터 정규화 후 거리 적용
        double scale = distance / length;
        double avoidX = dangerM.x + dx * scale;
        double avoidY = dangerM.y + dy * scale;

        ProjCoordinate avoidM = new ProjCoordinate(avoidX, avoidY);
        ProjCoordinate avoidWgs = new ProjCoordinate();
        TO_WGS84.transform(avoidM, avoidWgs);

        return new Point(avoidWgs.y, avoidWgs.x);
    }
}
