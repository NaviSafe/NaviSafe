package com.naviSafe.naviSafe.domain.outbreakCode.repository;

import com.naviSafe.naviSafe.domain.outbreakCode.entity.OutbreakCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutbreakCodeRepository extends JpaRepository<OutbreakCode, String> {
}
