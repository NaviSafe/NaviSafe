package com.ticker_now.weather_flow.domain.outbreakCode.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "ACC_TYPE")
    private String accType;

    @Column(name = "ACC_TYPE_NM")
    private String accTypeNM;
}
