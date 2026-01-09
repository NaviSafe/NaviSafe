package com.naviSafe.naviSafe.domain.MyRootPath.controller;

import com.naviSafe.naviSafe.domain.MyRootPath.dto.StartEndCoordRequestDto;
import com.naviSafe.naviSafe.domain.MyRootPath.service.MyRootPathService;
import com.naviSafe.naviSafe.domain.MyRootPath.service.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/naviSafe")
public class MyRootPathController {
    private final MyRootPathService myRootPathService;

    @PostMapping("/myRootPath")
    public ResponseEntity<List<Point>> getMyRootPath(@RequestBody StartEndCoordRequestDto startEndCoordRequestDto){
        List<Point> myRootPath = myRootPathService.getMyRootPath(
                startEndCoordRequestDto.getFromLongitude(), startEndCoordRequestDto.getFromLatitude(), startEndCoordRequestDto.getToLongitude(), startEndCoordRequestDto.getToLatitude()
        );
        return ResponseEntity.ok(myRootPath);
    }
}
