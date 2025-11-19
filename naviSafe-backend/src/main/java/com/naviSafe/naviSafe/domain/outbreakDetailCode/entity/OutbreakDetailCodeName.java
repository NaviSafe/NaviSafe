package com.naviSafe.naviSafe.domain.outbreakDetailCode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OUTBREAK_DETAIL_CODE_NAME")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakDetailCodeName {
    @Id
    @Column(name = "ACC_DTYPE")
    private String accDtype;

    @Column(name = "ACC_DTYPE_NM")
    private String accTypeNM;
}
