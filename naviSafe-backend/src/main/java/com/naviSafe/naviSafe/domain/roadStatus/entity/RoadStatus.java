package com.naviSafe.naviSafe.domain.roadStatus.entity;

import com.naviSafe.naviSafe.domain.regionCode.entity.RegionCode;
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

    @Column(name = "REG_CD_REG_CD", insertable = false, updatable = false)
    private int regCdRegCd;

    @Column(name = "ROAD_NAME")
    private String roadName;

    @Column(name = "ST_NODE_NM")
    private String startNodeNm;

    @Column(name = "ED_NODE_NM")
    private String endNodeNm;

    @Column(name = "MAP_DIST")
    private int mapDist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REG_CD_REG_CD", referencedColumnName = "REG_CD")
    private RegionCode regionCode;
}
