package com.naviSafe.naviSafe.domain.roadTraffic.repository;

import com.naviSafe.naviSafe.domain.roadTraffic.entity.RoadTraffic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadTrafficRepository extends JpaRepository<RoadTraffic, String> {
}
