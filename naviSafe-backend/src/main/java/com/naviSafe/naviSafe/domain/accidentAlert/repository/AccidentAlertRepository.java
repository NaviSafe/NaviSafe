package com.naviSafe.naviSafe.domain.accidentAlert.repository;

import com.naviSafe.naviSafe.domain.accidentAlert.entity.AccidentAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentAlertRepository extends JpaRepository<AccidentAlert, Long> {
}
