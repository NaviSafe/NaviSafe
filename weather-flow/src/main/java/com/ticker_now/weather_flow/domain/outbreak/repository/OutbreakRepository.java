package com.ticker_now.weather_flow.domain.outbreak.repository;

import com.ticker_now.weather_flow.domain.outbreak.entitiy.Outbreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutbreakRepository extends JpaRepository<Outbreak, String> {
    public Optional<Outbreak> findByAccId(String accId);
}
