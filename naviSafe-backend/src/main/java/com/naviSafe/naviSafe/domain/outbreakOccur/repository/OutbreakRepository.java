package com.naviSafe.naviSafe.domain.outbreakOccur.repository;

import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutbreakRepository extends JpaRepository<OutbreakOccur, String> {

    @Override
    @EntityGraph(
            attributePaths = {
                    "outbreakCode",
                    "outbreakCode.outbreakCodeName",
                    "outbreakDetailCode",
                    "outbreakDetailCode.outbreakDetailCodeName",
                    "outbreakMapGps",
                    "roadStatusLink",
                    "roadStatusLink.roadStatus",
                    "roadStatusLink.roadStatus.regionCode",
                    "accidentAlert"
            })
    List<OutbreakOccur> findAll();
}
