package com.naviSafe.naviSafe.domain.roadStatus.entitiy;

import com.naviSafe.naviSafe.domain.regionCode.entitiy.RegionCode;
import com.naviSafe.naviSafe.domain.roadTraffic.entitiy.RoadTraffic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LINK_ID")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoadStatus {
    @Id
    @Column(name = "LINK_ID")
    private String linkId;

    @Column(name = "ROAD_NAME")
    private String roadName;

    @Column(name = "ST_NODE_NM")
    private String startNodeNm;

    @Column(name = "ED_NODE_NM")
    private String endNodeNm;

    @Column(name = "MAP_DIST")
    private int mapDist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LINK_ID", insertable = false, updatable = false)
    private RoadTraffic roadTraffic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REG_CD")
    private RegionCode regionCode;
}
