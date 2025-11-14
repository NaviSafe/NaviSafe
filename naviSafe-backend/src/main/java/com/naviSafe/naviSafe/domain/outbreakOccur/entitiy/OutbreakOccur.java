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
@Table(name = "OUTBREAK_OCCURRENCE")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OutbreakOccur {
    @Id
    @Column(name = "ACC_ID")
    private String accId;

    @Column(name = "occr_date_time")
    private ZonedDateTime occrDate;

    @Column(name = "exp_clr_date_time")
    private ZonedDateTime expClrDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_ID", referencedColumnName = "OUTBREAK_ACC_ID")
    private OutbreakCode outbreakCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_ID", referencedColumnName = "OUTBREAK_ACC_ID")
    private OutbreakDetailCode outbreakDetailCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_ID", referencedColumnName = "OUTBREAK_ACC_ID")
    private OutbreakMapGps outbreakMapGps;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_ID", referencedColumnName = "OUTBREAK_ACC_ID")
    private RoadStatusLink roadStatusLink;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_ID", referencedColumnName = "OUTBREAK_ACC_ID")
    private AccidentAlert accidentAlert;


}
