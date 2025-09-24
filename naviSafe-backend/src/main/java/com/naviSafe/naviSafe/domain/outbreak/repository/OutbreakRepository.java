package com.naviSafe.naviSafe.domain.outbreak.repository;

import com.naviSafe.naviSafe.domain.outbreak.entitiy.Outbreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutbreakRepository extends JpaRepository<Outbreak, String> {
    public Optional<Outbreak> findByAccId(String accId);
}
