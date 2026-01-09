package com.naviSafe.naviSafe.domain.MyRootPath.service;

public class GeoCalculator {

    public static double distance(Point p1, Point p2) {
        double lat1 = Math.toRadians(p1.lat());
        double lon1 = Math.toRadians(p1.lon());
        double lat2 = Math.toRadians(p2.lat());
        double lon2 = Math.toRadians(p2.lon());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return 6371000 * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
