package com.naviSafe.naviSafe.domain.MyRootPath.v2.service;


import com.naviSafe.naviSafe.domain.MyRootPath.v2.dto.Point;
import com.naviSafe.naviSafe.domain.MyRootPath.v2.dto.RouteResult;
import com.naviSafe.naviSafe.domain.MyRootPath.v2.repository.RouteRepository;
import com.naviSafe.naviSafe.domain.MyRootPath.v2.utils.GeoCoordinateConverter;
import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.service.OutbreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final OutbreakService outbreakService;
    private final RouteRepository routeRepository;
    private final GeoCoordinateConverter geoCoordinateConverter;

    @Autowired
    public RouteService(OutbreakService outbreakService,
                        RouteRepository routeRepository,
                        @Qualifier("geoConverterV2") GeoCoordinateConverter geoCoordinateConverter) {
        this.outbreakService = outbreakService;
        this.routeRepository = routeRepository;
        this.geoCoordinateConverter = geoCoordinateConverter;
    }

    public RouteResult getRoute(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude){
        List<OutbreakOccur> outbreakOccurs = outbreakService.findAll();

        List<Point> list = outbreakOccurs.stream()
                .map(occ -> new Point(occ.getOutbreakMapGps().getGrs80tmX(), occ.getOutbreakMapGps().getGrs80tmY()))
                .toList();

        List<Point> dangerPoints = outbreakOccurs.stream()
                .map(o -> geoCoordinateConverter.convert(
                        o.getOutbreakMapGps().getGrs80tmX(), // GRS80TM X
                        o.getOutbreakMapGps().getGrs80tmY()  // GRS80TM Y
                ))
                .toList();

        return routeRepository.findRoute(fromLongitude, fromLatitude, toLongitude, toLatitude, dangerPoints);
    }
}
