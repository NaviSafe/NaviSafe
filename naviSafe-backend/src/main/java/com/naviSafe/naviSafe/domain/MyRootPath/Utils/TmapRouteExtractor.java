package com.naviSafe.naviSafe.domain.MyRootPath.Utils;

import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapFeature;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteResponse;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.geometry.TmapLineStringGeometry;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.geometry.TmapPointGeometry;
import com.naviSafe.naviSafe.domain.MyRootPath.service.Point;

import java.util.ArrayList;
import java.util.List;

public class TmapRouteExtractor {

    public static List<Point> extractPolyline(TmapRouteResponse response) {
        List<Point> result = new ArrayList<>();

        for (TmapFeature feature : response.getFeatures()) {
            if(feature.getGeometry() instanceof TmapPointGeometry && feature.getProperties().getDescription().equals("도착")) {
                break;
            }
            
            if (!(feature.getGeometry() instanceof TmapLineStringGeometry line)) {
                continue;
            }

            for (List<Double> coord : line.getCoordinates()) {
                double lon = coord.get(0);
                double lat = coord.get(1);

                Point point = new Point(lat, lon);
                result.add(point);
            }
        }

        return result;
    }
}
