package com.ticker_now.weather_flow.domain.regionCode.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REG_CD")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegionCode {
    @Id
    @Column(name = "REG_CD")
    private int regCode;

    @Column(name = "REG_NAME")
    private String regName;
}
