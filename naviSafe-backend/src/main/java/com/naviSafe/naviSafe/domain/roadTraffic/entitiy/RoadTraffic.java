package com.naviSafe.naviSafe.domain.roadTraffic.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ROAD_TRAFFIC")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoadTraffic {
    @Id
    @Column(name = "LINK_ID")
    private String linkId;

    @Column(name = "PRCS_SPD")
    private String prcsSpd;

    @Column(name = "PRCS_TRV_TIME")
    private String prcsTrvTime;
}
