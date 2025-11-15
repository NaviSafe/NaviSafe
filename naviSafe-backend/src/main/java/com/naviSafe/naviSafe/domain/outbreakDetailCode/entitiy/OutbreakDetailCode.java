package com.naviSafe.naviSafe.domain.outbreakDetailCode.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "OUTBREAK_DETAIL_CODE")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakDetailCode {
    @Id
    @Column(name = "OUTBREAK_ACC_ID")
    private String outbreakAccId;

    @Column(name = "ACC_DTYPE")
    private String accDtype;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_DTYPE", insertable = false, updatable = false)
    private OutbreakDetailCodeName outbreakDetailCodeName;
}
