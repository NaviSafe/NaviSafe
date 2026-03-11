package com.naviSafe.naviSafe.domain.MyRootPath.v2.repository;

import com.naviSafe.naviSafe.domain.MyRootPath.v1.service.DangerZoneSelector;
import com.naviSafe.naviSafe.domain.MyRootPath.v1.service.Point;
import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.service.OutbreakService;
import com.naviSafe.naviSafe.domain.MyRootPath.v1.service.GeoCoordinateConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RouteRepositoryTest {
    private final OutbreakService outbreakService;
    private final RouteRepository routeRepository;
    private final GeoCoordinateConverter converter;

    Logger logger = LoggerFactory.getLogger(RouteRepositoryTest.class);
    @Autowired
    public RouteRepositoryTest(OutbreakService outbreakService, RouteRepository routeRepository, GeoCoordinateConverter converter) {
        this.outbreakService = outbreakService;
        this.routeRepository = routeRepository;
        this.converter = converter;
    }

    @Test
    @DisplayName("신규 라우팅 테스트")
    void test() throws Exception {
        List<OutbreakOccur> outbreakOccurs = outbreakService.findAll();

        List<Point> list = outbreakOccurs.stream()
                .map(occ -> new Point(occ.getOutbreakMapGps().getGrs80tmX(), occ.getOutbreakMapGps().getGrs80tmY()))
                .toList();

        List<Point> dangerPoints = outbreakOccurs.stream()
                .map(o -> converter.convert(
                        o.getOutbreakMapGps().getGrs80tmX(), // GRS80TM X
                        o.getOutbreakMapGps().getGrs80tmY()  // GRS80TM Y
                ))
                .collect(Collectors.toList());

        dangerPoints.add(new Point(37.56279828504156,126.96203884741068));


        for (Point dangerPoint : dangerPoints) {
            logger.info("dangerPoint : {}", dangerPoint.toString());
        }

        List<Point> route = routeRepository.findRoute(126.963826124, 37.559915864, 126.972668997, 37.550635339, dangerPoints);

        StringBuilder html = new StringBuilder("""
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="utf-8" />
                <title>Danger Visualization</title>
                <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
                </head>
                <body>
                <div id="map" style="width:100%;height:100vh;"></div>
                <script>
                const map = L.map('map').setView([37.5665, 126.9780], 11);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                """);

        // 🔴 원본 돌발상황 (빨간 원)
        for (Point danger : dangerPoints) {
            html.append("L.circle([")
                    .append(danger.lat()).append(", ").append(danger.lon())
                    .append("], { color:'red', fillColor:'#f03', fillOpacity:0.5, radius:50 }).addTo(map);\n");
        }

        html.append("const latlngs = [\n");
        for (Point p : route) {
            html.append("[").append(p.lat()).append(", ").append(p.lon()).append("],\n");
        }
        html.append("];\n");
        html.append("L.polyline(latlngs, {color: 'blue'}).addTo(map);\n");

        html.append("</script></body></html>");

        Files.writeString(Path.of("danger-centers EPS-"+ DangerZoneSelector.EPS +".html"), html);
    }
}