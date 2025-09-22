package com.ticker_now.weather_flow.domain.outbreakDetailCode.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OUTBREAK_DETAIL_CODE")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakDetailCode {
    @Id
    @Column(name = "ACC_DTYPE")
    private String accType;

    @Column(name = "ACC_DTYPE_NM")
    private String accTypeNM;
}
