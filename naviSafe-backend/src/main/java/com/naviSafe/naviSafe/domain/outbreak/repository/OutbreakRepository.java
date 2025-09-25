package com.naviSafe.naviSafe.domain.outbreak.repository;

import com.naviSafe.naviSafe.domain.outbreak.entitiy.Outbreak;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutbreakRepository extends JpaRepository<Outbreak, String> {

    @Override
    @EntityGraph(attributePaths = {"outbreakCode", "outbreakDetailCode", "roadStatus","roadStatus.regionCode", "roadStatus.roadTraffic"})
    List<Outbreak> findAll();
    Optional<Outbreak> findByAccId(String accId);
}
