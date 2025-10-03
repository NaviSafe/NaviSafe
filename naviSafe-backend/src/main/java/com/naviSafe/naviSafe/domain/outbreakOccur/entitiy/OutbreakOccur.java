package com.naviSafe.naviSafe.domain.outbreakOccur.entitiy;

import com.naviSafe.naviSafe.domain.accidentAlert.entity.AccidentAlert;
import com.naviSafe.naviSafe.domain.outbreakCode.entitiy.OutbreakCode;
import com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy.OutbreakDetailCode;
import com.naviSafe.naviSafe.domain.outbreakMapGPS.entity.OutbreakMapGps;
import com.naviSafe.naviSafe.domain.roadStatus.entitiy.RoadStatusLink;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "OUTBREAK_OCCURENCE")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OutbreakOccur {
    @Id
    @Column(name = "ACC_ID")
    private String accId;

    @Column(name = "OCCR_DATE")
    private ZonedDateTime occrDate;

    @Column(name = "EXP_CLR_DATE")
    private ZonedDateTime expClrDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTBREAK_ACC_ID")
    private OutbreakCode outbreakCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTBREAK_ACC_ID", insertable = false, updatable = false)
    private OutbreakDetailCode outbreakDetailCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTBREAK_ACC_ID", insertable = false, updatable = false)
    private OutbreakMapGps outbreakMapGps;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTBREAK_ACC_ID", insertable = false, updatable = false)
    private RoadStatusLink roadStatusLink;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTBREAK_ACC_ID", insertable = false, updatable = false)
    private AccidentAlert accidentAlert;


}
