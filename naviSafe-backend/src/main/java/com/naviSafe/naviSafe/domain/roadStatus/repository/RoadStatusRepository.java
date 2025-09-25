package com.naviSafe.naviSafe.domain.roadStatus.repository;

import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatus;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoadStatusRepository extends JpaRepository<RoadStatus, String> {
    @Query("select r from RoadStatus r left join fetch r.regionCode")
    List<RoadStatus> findRoadStatusFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"regionCode", "roadTraffic"})
    List<RoadStatus> findAll();
}


