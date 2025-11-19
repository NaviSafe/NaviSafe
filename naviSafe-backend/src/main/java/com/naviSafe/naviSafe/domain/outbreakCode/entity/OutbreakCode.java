package com.naviSafe.naviSafe.domain.outbreakCode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OUTBREAK_CODE")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakCode {

    @Id
    @Column(name = "OUTBREAK_ACC_ID")
    private String outbreakAccId;

    @Column(name = "ACC_TYPE")
    private String accType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACC_TYPE", insertable = false, updatable = false)
    private OutbreakCodeName outbreakCodeName;
}
