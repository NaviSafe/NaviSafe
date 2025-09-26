package com.naviSafe.naviSafe.domain.rain.repository;

import com.naviSafe.naviSafe.domain.rain.entitiy.Rain;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RainRepository extends JpaRepository<Rain, Integer> {

    @Override
    @EntityGraph(attributePaths = {"region"})
    List<Rain> findAll();
}
