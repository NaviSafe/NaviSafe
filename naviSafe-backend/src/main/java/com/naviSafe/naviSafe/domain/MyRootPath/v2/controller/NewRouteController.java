package com.naviSafe.naviSafe.domain.MyRootPath.v2.controller;

import com.naviSafe.naviSafe.domain.MyRootPath.v2.dto.Point;
import com.naviSafe.naviSafe.domain.MyRootPath.v2.dto.StartEndCoordRequestDto;
import com.naviSafe.naviSafe.domain.MyRootPath.v2.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/naviSafe")
public class NewRouteController {
    private final RouteService routeService;

    @PostMapping("/myRootPath_v2")
    public ResponseEntity<List<Point>> getMyRootPath(@RequestBody StartEndCoordRequestDto startEndCoordRequestDto){
        List<Point> myRootPath = routeService.getRoute(
                startEndCoordRequestDto.getFromLongitude(), startEndCoordRequestDto.getFromLatitude(), startEndCoordRequestDto.getToLongitude(), startEndCoordRequestDto.getToLatitude()
        );
        return ResponseEntity.ok(myRootPath);
    }
}
