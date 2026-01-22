package com.naviSafe.naviSafe.domain.MyRootPath.service;

import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteResponse;
import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.service.OutbreakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class MyRootPathServiceTest {

    private final MyRootPathService myRootPathService;
    private final GeoCoordinateConverter converter;
    private final Logger logger = LoggerFactory.getLogger(MyRootPathServiceTest.class);

    @MockitoBean
    private OutbreakService outbreakService;



    @Autowired
    public MyRootPathServiceTest(MyRootPathService myRootPathService, GeoCoordinateConverter converter) {
        this.myRootPathService = myRootPathService;
        this.converter = converter;
    }

    @BeforeEach
    void setUp() {
        OutbreakOccur occur1 = TestOutbreakOccur.create("1", 192385.0401850186, 446645.5247824667);
        OutbreakOccur occur2 = TestOutbreakOccur.create("2", 197317.195, 451389.51);
        OutbreakOccur occur3 = TestOutbreakOccur.create("3", 206318.237564, 445199.529199);
        OutbreakOccur occur4 = TestOutbreakOccur.create("4", 183879.9844091554, 453619.9312908591);
        OutbreakOccur occur5 = TestOutbreakOccur.create("5", 197806.4725260006, 451696.3851346326);
        OutbreakOccur occur6 = TestOutbreakOccur.create("6", 184594.3687499735, 452478.1499938662);
        OutbreakOccur occur7 = TestOutbreakOccur.create("7", 197941.2938470728, 452234.9821773331);
        OutbreakOccur occur8 = TestOutbreakOccur.create("8", 201211.381847, 443806.488156);
        OutbreakOccur occur9 = TestOutbreakOccur.create("9", 200937.631847, 443008.488156);
        OutbreakOccur occur10 = TestOutbreakOccur.create("10", 211251.881847, 441513.488156);
        OutbreakOccur occur11 = TestOutbreakOccur.create("11", 184867.131847, 452445.488156);
        OutbreakOccur occur12 = TestOutbreakOccur.create("12", 211218.631847, 441693.488156);
        OutbreakOccur occur13 = TestOutbreakOccur.create("13", 200917.631847, 443024.488156);
        OutbreakOccur occur14 = TestOutbreakOccur.create("14", 202707.881847, 456418.488156);

        given(outbreakService.findAll())
                .willReturn(List.of(occur1, occur2, occur3, occur4, occur5, occur6, occur7, occur8, occur9, occur10, occur11, occur12, occur13, occur14));
//                .willReturn(List.of(occur1));
    }

    @Test
    @DisplayName("돌발상황을 대표하는 경유지 생성 테스트")
    void getDangerCentersVisual() throws Exception {
        List<OutbreakOccur> occurs = outbreakService.findAll();
        List<Point> dangerPoints = occurs.stream()
                .map(o -> converter.convert(
                        o.getOutbreakMapGps().getGrs80tmX(), // GRS80TM X
                        o.getOutbreakMapGps().getGrs80tmY()  // GRS80TM Y
                ))
                .toList();

        for (Point dangerPoint : dangerPoints) {
            logger.info("dangerPoint : {}", dangerPoint.toString());
        }

        List<Point> testPoints = List.of(new Point(0, 0));
        List<Point> points = myRootPathService.generateDangerCenters(testPoints);

        for (Point point : points) {
            logger.info("예상 경유지 : {}", point.toString());
        }

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
                    .append(danger.lat())
                    .append(", ")
                    .append(danger.lon())
                    .append("], {")
                    .append(" color: 'red',")
                    .append(" fillColor: '#f03',")
                    .append(" fillOpacity: 0.5,")
                    .append(" radius: 50")
                    .append(" }).addTo(map);\n");
        }

        // 🔵 대표 위험 중심점
        for (Point p : points) {
            html.append("""
                            L.marker([""")
                    .append(p.lat())
                    .append(", ")
                    .append(p.lon())
                    .append("]).addTo(map);\n");
        }

        html.append("""
                </script>
                </body>
                </html>
                """);


        Files.writeString(Path.of("danger-centers EPS-"+ DangerZoneSelector.EPS +".html"), html);
    }


    @Test
    @DisplayName("돌발상황 우휘 최단경로 생성")
    void getRootPath() throws Exception {
//        1. 기본 좌표
//        단일 돌발상황 tc
//        double fromLon = 126.913606817; // 근로복지공단 서울남부지사
//        double fromLat = 37.520394201;
//        double toLon = 126.908472738; // 한국산업인력공단 서울남부지사
//        double toLat = 37.528002405;

//        여러 돌발상황 tc
        double fromLon = 126.985834394; // 해정병원
        double fromLat = 37.575570097;
        double toLon = 126.957262441; // 아현감리교회
        double toLat = 37.560940645;

        // 기존 경로 polyline 추출
        List<Point> myOriginalPath = myRootPathService.getMyOriginalPath(fromLon, fromLat, toLon, toLat);

        // 새로운 경로 polyline 추출
        List<Point> myRootPath = myRootPathService.getMyRootPathTest(fromLon, fromLat, toLon, toLat, myOriginalPath);

        // 3. 돌발상황 및 경유지 생성
        List<OutbreakOccur> occurs = outbreakService.findAll();
        List<Point> dangerPoints = occurs.stream()
                .map(o -> converter.convert(
                        o.getOutbreakMapGps().getGrs80tmX(), // GRS80TM X
                        o.getOutbreakMapGps().getGrs80tmY()  // GRS80TM Y
                ))
                .toList();

        List<Point> viaPoints = myRootPathService.generateDangerCenters(myOriginalPath);

        logger.info("viaPoints : {}", viaPoints);

        // 4. HTML 시각화
        StringBuilder html = new StringBuilder("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8" />
            <title>Root Path + Dangers Visualization</title>
            <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>
            <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
            </head>
            <body>
            <div id="map" style="width:100%;height:100vh;"></div>
            <script>
            const map = L.map('map').setView([37.5665, 126.9780], 13);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
            """);

        // 🔴 돌발상황 원
        for (Point danger : dangerPoints) {
            html.append("L.circle([")
                    .append(danger.lat()).append(", ").append(danger.lon())
                    .append("], { color:'red', fillColor:'#f03', fillOpacity:0.5, radius:50 }).addTo(map);\n");
        }

        // 🔵 경유지 / 위험 중심점 마커
        for (Point p : viaPoints) {
            html.append("L.marker([")
                    .append(p.lat()).append(", ").append(p.lon())
                    .append("]).addTo(map);\n");
        }

        // 초기 경로 polyline
        html.append("const latlng_origins = [\n");
        for (Point p : myOriginalPath) {
            html.append("[").append(p.lat()).append(", ").append(p.lon()).append("],\n");
        }
        html.append("];\n");
        html.append("L.polyline(latlng_origins, {color: 'green'}).addTo(map);\n");

        // 최종 경로 polyline
        html.append("const latlngs = [\n");
        for (Point p : myRootPath) {
            html.append("[").append(p.lat()).append(", ").append(p.lon()).append("],\n");
        }
        html.append("];\n");
        html.append("L.polyline(latlngs, {color: 'blue'}).addTo(map);\n");

        html.append("</script></body></html>");

        Files.writeString(Path.of("root-path-with-dangers.html"), html);

        logger.info("myRootPath: {}", myRootPath);
    }
}
