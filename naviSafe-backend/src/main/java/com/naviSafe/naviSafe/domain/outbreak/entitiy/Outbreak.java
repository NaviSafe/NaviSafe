package com.naviSafe.naviSafe.domain.outbreak.entitiy;

import com.naviSafe.naviSafe.domain.outbreakCode.entitiy.OutbreakCode;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCode;
import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "OUTBREAK")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Outbreak {
    @Id
    @Column(name = "ACC_ID")
    private String accId;

    @Column(name = "OCCR_DATE")
    private ZonedDateTime occrDate;

    @Column(name = "EXP_CLR_DATE")
    private ZonedDateTime expClrDate;

    @Column(name = "ACC_INFO")
    private String accInfo;

    @Column(name = "GRS80TM_X")
    private float grs80tmX;

    @Column(name = "GRS80TM_Y")
    private float grs80tmY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LINK_ID")
    private RoadStatus roadStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_TYPE")
    private OutbreakCode outbreakCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_DTYPE")
    private OutbreakDetailCode outbreakDetailCode;
}
