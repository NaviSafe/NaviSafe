package com.naviSafe.naviSafe.domain.region.repository;

import com.naviSafe.naviSafe.domain.region.entitiy.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Integer> {
}
