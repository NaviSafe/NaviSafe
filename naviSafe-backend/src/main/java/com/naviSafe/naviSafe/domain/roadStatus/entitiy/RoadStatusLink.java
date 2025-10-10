package com.naviSafe.naviSafe.domain.roadStatus.entitiy;

import com.naviSafe.naviSafe.domain.roadTraffic.entitiy.RoadTraffic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "OUTBREAK_LINK")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoadStatusLink {
    @Id
    @Column(name = "OUTBREAK_ACC_ID")
    private String outbreakAccId;

    @Column(name = "LINK_ID_LINK_ID")
    private String linkIdLinkId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LINK_ID")
    private RoadStatus roadStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LINK_ID", insertable = false, updatable = false)
    private RoadTraffic roadTraffic;
}
