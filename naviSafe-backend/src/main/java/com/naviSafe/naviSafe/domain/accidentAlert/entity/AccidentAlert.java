package com.naviSafe.naviSafe.domain.accidentAlert.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ACC_ALERTS")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccidentAlert {

    @Id
    @Column(name = "OUTBREAK_ACC_ID")
    private String outbreakAccId;

    @Column(name = "ACC_INFO")
    private String accInfo;
}
