package com.naviSafe.naviSafe.domain.outbreakMapGPS.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "MAP_GPS")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakMapGps {

    @Id
    @Column(name = "OUTBREAK_ACC_ID")
    private String outbreakAccId;

    @Column(name = "GRS80TM_X")
    private double grs80tmX;

    @Column(name = "GRS80TM_Y")
    private double grs80tmY;
}
