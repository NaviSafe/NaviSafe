package com.naviSafe.naviSafe.domain.MyRootPath.service;

import com.naviSafe.naviSafe.domain.MyRootPath.Utils.TmapRequestUtil;
import com.naviSafe.naviSafe.domain.MyRootPath.Utils.TmapRouteExtractor;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.TmapApiClient;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteRequest;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteResponse;
import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.service.OutbreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyRootPathService {
    private final OutbreakService outbreakService;
    private final GeoCoordinateConverter converter;
    private final DangerZoneSelector dangerZoneSelector;
    private final TmapApiClient tmapApiClient;
    private final Logger logger = LoggerFactory.getLogger(MyRootPathService.class);

    public List<Point> generateDangerCenters(List<Point> points) {
        List<OutbreakOccur> occurs = outbreakService.findAll();

        if (occurs.isEmpty()) return List.of();

        List<Point> dangerPoints = occurs.stream()
                .map(o -> converter.convert(
                        o.getOutbreakMapGps().getGrs80tmX(), // GRS80TM X
                        o.getOutbreakMapGps().getGrs80tmY()  // GRS80TM Y
                ))
                .toList();
        return dangerZoneSelector.selectAvoidPoints(dangerPoints, points);
    }



    public List<Point> getMyRootPath(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
        TmapRouteRequest routeRequest = TmapRouteRequest.builder()
                .startX(fromLongitude)
                .startY(fromLatitude)
                .endX(toLongitude)
                .endY(toLatitude)
                .build();

        TmapRouteResponse tmapRoute = tmapApiClient.getTmapRoute(routeRequest);
        List<Point> polyLinesPoints = TmapRouteExtractor.extractPolyline(tmapRoute);
        List<Point> dangerCenters = generateDangerCenters(polyLinesPoints);

        for (Point dangerCenter : dangerCenters) {
            logger.info("생성된 경유지: {}", dangerCenter);
        }

        if (dangerCenters.isEmpty()) {
            logger.info("생성된 경유지가 없으므로 기존 경로를 반환합니다.");
            return polyLinesPoints;
        }

        TmapRouteRequest newRouteRequest = TmapRouteRequest.builder()
                .startX(fromLongitude)
                .startY(fromLatitude)
                .endX(toLongitude)
                .endY(toLatitude)
                .passList(TmapRequestUtil.buildPassListString(dangerCenters))
                .build();

        TmapRouteResponse newTmapRoute = tmapApiClient.getTmapRoute(newRouteRequest);
        return TmapRouteExtractor.extractPolyline(newTmapRoute);
    }

    public List<Point> getMyOriginalPath(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
        TmapRouteRequest routeRequest = TmapRouteRequest.builder()
                .startX(fromLongitude)
                .startY(fromLatitude)
                .endX(toLongitude)
                .endY(toLatitude)
                .build();

        TmapRouteResponse tmapRoute = tmapApiClient.getTmapRoute(routeRequest);
        return TmapRouteExtractor.extractPolyline(tmapRoute);
    }

    public List<Point> getMyRootPathTest(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude, List<Point> originalPolyLinesPoints) {
        List<Point> polyLinesPoints;

        if(originalPolyLinesPoints.isEmpty()){
            TmapRouteRequest routeRequest = TmapRouteRequest.builder()
                    .startX(fromLongitude)
                    .startY(fromLatitude)
                    .endX(toLongitude)
                    .endY(toLatitude)
                    .build();

            TmapRouteResponse tmapRoute = tmapApiClient.getTmapRoute(routeRequest);
            polyLinesPoints = TmapRouteExtractor.extractPolyline(tmapRoute);
        }else{
            polyLinesPoints = originalPolyLinesPoints;
        }


        List<Point> dangerCenters = generateDangerCenters(polyLinesPoints);

        for (Point dangerCenter : dangerCenters) {
            logger.info("생성된 경유지: {}", dangerCenter);
        }

        if (dangerCenters.isEmpty()) {
            logger.info("생성된 경유지가 없으므로 기존 경로를 반환합니다.");
            return polyLinesPoints;
        }

        TmapRouteRequest newRouteRequest = TmapRouteRequest.builder()
                .startX(fromLongitude)
                .startY(fromLatitude)
                .endX(toLongitude)
                .endY(toLatitude)
                .passList(TmapRequestUtil.buildPassListString(dangerCenters))
                .build();

        TmapRouteResponse newTmapRoute = tmapApiClient.getTmapRoute(newRouteRequest);
        return TmapRouteExtractor.extractPolyline(newTmapRoute);
    }
}
