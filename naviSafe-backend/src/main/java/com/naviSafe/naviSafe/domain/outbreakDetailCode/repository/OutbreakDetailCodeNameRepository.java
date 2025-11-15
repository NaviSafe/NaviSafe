package com.naviSafe.naviSafe.domain.outbreakDetailCode.repository;

import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCode;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCodeName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutbreakDetailCodeNameRepository extends JpaRepository<OutbreakDetailCodeName, String> {
}
