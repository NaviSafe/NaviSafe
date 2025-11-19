package com.naviSafe.naviSafe.domain.shelter.type.repository;

import com.naviSafe.naviSafe.domain.shelter.type.entity.ShelterType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShelterTypeRepository extends JpaRepository<ShelterType, Long> {

    @EntityGraph(
            attributePaths = {
                    "shelterGpsList"
            })
    Optional<ShelterType> findByShelterCode(int code);
}
