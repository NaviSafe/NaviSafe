package com.naviSafe.naviSafe.domain.MyRootPath.service;

import org.springframework.stereotype.Component;
import org.locationtech.proj4j.*;

@Component
public class GeoCoordinateConverter {

    private final CoordinateTransform transform;

    public GeoCoordinateConverter() {

        CRSFactory crsFactory = new CRSFactory();

        // EPSG:2097
        CoordinateReferenceSystem epsg2097 =
                crsFactory.createFromParameters(
                        "EPSG:2097",
                        "+proj=tmerc +lat_0=38 +lon_0=127 " +
                                "+k=1 +x_0=200000 +y_0=500000 " +
                                "+ellps=bessel +units=m +no_defs " +
                                "+towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43"
                );

        CoordinateReferenceSystem wgs84 =
                crsFactory.createFromName("EPSG:4326");

        CoordinateTransformFactory ctFactory =
                new CoordinateTransformFactory();

        this.transform =
                ctFactory.createTransform(epsg2097, wgs84);
    }

    public Point convert(double x, double y) {
        ProjCoordinate src = new ProjCoordinate(x, y);
        ProjCoordinate dst = new ProjCoordinate();
        transform.transform(src, dst);

        return new Point(dst.y, dst.x); // lat, lon
    }
}