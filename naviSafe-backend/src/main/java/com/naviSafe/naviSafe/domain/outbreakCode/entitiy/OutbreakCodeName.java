package com.naviSafe.naviSafe.domain.outbreakCode.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OUTBREAK_NAME")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakCodeName {
    @Id
    @Column(name = "ACC_TYPE")
    private String accType;

    @Column(name = "ACC_TYPE_NM")
    private String accTypeNM;
}
